/* ChatNotes: observe a bounded CLONE, never consume/cancel the page's response. */
(function () {
  'use strict';
  if (window.top !== window || window.__chatnotes) return;
  var MAX_TEXT = 180000, MAX_STREAM = 8 * 1024 * 1024, MAX_JSON = 4 * 1024 * 1024;
  var latest = Object.create(null), contexts = [], localId = uid('local:'), activeId = '';
  var lastRoute = '', networkMessages = 0;
  function uid(prefix) {
    return prefix + (window.crypto && window.crypto.randomUUID ? window.crypto.randomUUID() :
      Date.now().toString(36) + Math.random().toString(36).slice(2));
  }
  function send(obj) {
    try { window.chatnotesProxy.postMessage(JSON.stringify(obj)); } catch (_) { /* bridge absent */ }
  }
  function notice(text) { send({type: 'notice', message: text}); }
  function currentUrl() { return String(window.location.href); }
  function routeId() {
    var m = new URL(currentUrl()).pathname.match(/\/c\/([a-zA-Z0-9_-]+)/);
    return m ? m[1] : '';
  }
  function activate(cid) {
    activeId = cid;
    send({type: 'active', conversationId: cid, url: currentUrl()});
  }
  function routeChanged() {
    var path = new URL(currentUrl()).pathname;
    if (lastRoute !== path) {
      if (!routeId()) localId = uid('local:');
      lastRoute = path;
      activate(routeId() || localId);
      scheduleTitleSync();
    }
  }

  // ---- Title fallback (new ChatGPT UI may never call /title endpoints) ----
  var titleTimer = 0, lastTitle = '';
  function extractDomTitle() {
    if (!routeId()) return;
    // Sidebar link for the current conversation carries its title.
    var link = window.document.querySelector(
      'a[href*="/c/' + routeId() + '"][aria-label], a[data-testid*="conversation"][href*="' + routeId() + '"]');
    var label = '';
    try { label = (link && link.getAttribute('aria-label') || '').replace(/^\s*(对话|Chat)\s*[:：]?\s*/i, '').trim(); } catch (_) {}
    if (!label) {
      // document.title is usually the conversation topic on chatgpt.com.
      var t = String(window.document.title || '').trim();
      if (t && !/^(chatgpt|new chat|新对话|登录|log in)/i.test(t)) label = t;
    }
    if (label && label !== lastTitle) {
      lastTitle = label;
      send({type: 'conversation', conversationId: routeId(), title: label.slice(0, 200)});
    }
  }
  function scheduleTitleSync() {
    if (titleTimer) return;
    // Poll briefly after navigation: the sidebar and title settle asynchronously.
    var tries = 0;
    titleTimer = setInterval(function () {
      tries++;
      extractDomTitle();
      if (tries >= 6 || lastTitle) { clearInterval(titleTimer); titleTimer = 0; }
    }, 1000);
  }
  ['pushState', 'replaceState'].forEach(function (key) {
    var original = window.history[key];
    window.history[key] = function () {
      var result = original.apply(this, arguments);
      routeChanged();
      return result;
    };
  });
  window.addEventListener('popstate', routeChanged);
  routeChanged();
  scheduleTitleSync();

  function extract(content) {
    var parts = content && content.parts, texts = [], attachments = false;
    if (Array.isArray(parts)) parts.forEach(function (p) {
      if (typeof p === 'string') texts.push(p);
      else if (p && typeof p.text === 'string') texts.push(p.text);
      else attachments = true;
    });
    else if (content && typeof content.text === 'string') texts.push(content.text);
    else attachments = !!content;
    if (attachments) texts.push('[附件或非文本内容未采集，请在原平台查看]');
    var text = texts.join('\n'), truncated = text.length > MAX_TEXT;
    if (truncated) {
      text = text.slice(0, MAX_TEXT) + '\n[内容超过单条采集上限，原文不完整]';
      notice('单条消息过长，已保存部分内容；请分段导入。');
    }
    return {text: text, attachments: attachments || truncated};
  }
  function emit(ctx, m, status, source, parent, select) {
    if (ctx.closed) return false;
    if (!m || !m.id || !m.author || !/^(user|assistant)$/.test(m.author.role)) return false;
    if (m.channel === 'analysis' || (m.metadata && m.metadata.is_visually_hidden_from_conversation)) return false;
    var ct = m.content && m.content.content_type || '';
    // Thinking/reasoning containers carry no displayable text and no attachments:
    // they are NOT "attachments" — emitting a placeholder for them inflates the
    // capture count (user report: one Q&A counted 3 messages).
    if (/^(thinking|reasoning|multimodal_thinking|thought|plan)$/i.test(ct)) return false;
    var content = extract(m.content);
    // An empty non-text container with no real attachment metadata is skipped.
    if (!content.text.replace(/\[附件或非文本内容未采集.*?\]/g, '').trim() &&
        !(m.metadata && Array.isArray(m.metadata.attachments) && m.metadata.attachments.length))
      return false;
    if (m.metadata && Array.isArray(m.metadata.attachments) && m.metadata.attachments.length) {
      content.attachments = true;
      content.text += '\n[附带文件未采集，请在原平台查看]';
    }
    if (!content.text) return false;
    send({type: 'message', conversationId: ctx.cid, msgId: String(m.id),
      parentId: String(parent || ''), role: m.author.role, text: content.text,
      status: status, source: source, attachments: content.attachments,
      createdAt: m.create_time ? Math.round(m.create_time * 1000) : ctx.time,
      select: !!select, uncertain: !!ctx.unsupported});
    return true;
  }
  function bind(ctx, cid) {
    if (!cid || cid === ctx.cid) return;
    var old = ctx.cid;
    // A server-assigned ID may replace only a local provisional ID.
    if (old.indexOf('local:') !== 0) return;
    send({type: 'remap', from: old, to: cid});
    if (latest[old]) { latest[cid] = latest[old]; delete latest[old]; }
    contexts.forEach(function (c) { if (c.cid === old) c.cid = cid; });
    ctx.cid = cid;
    if (activeId === old) activate(cid);
  }
  function publish(ctx, item, status) {
    var chosen = latest[ctx.cid] === ctx.token;
    if (emit(ctx, item.message, status, 'network', item.parent, chosen)) networkMessages++;
    item.sentAt = Date.now();
  }
  function flush(ctx, status) {
    Object.keys(ctx.answers).forEach(function (id) {
      var item = ctx.answers[id];
      publish(ctx, item, status || item.status);
    });
  }
  function prepare(input, init, ctx) {
    function parse(text) {
      if (ctx.closed) return;
      if (!text || text.length > MAX_JSON) return;
      try {
        var j = JSON.parse(text);
        var initialId = ctx.cid;
        ctx.cid = j.conversation_id || initialId;
        ctx.parent = j.parent_message_id || '';
        latest[ctx.cid] = ctx.token;
        if (activeId === initialId) activate(ctx.cid);
        (j.messages || []).forEach(function (m) {
          var role = m.author && m.author.role || m.role || 'user';
          if (role !== 'user') return;
          var user = {id: m.id || uid('request:'), author: {role: 'user'}, content: m.content, metadata: m.metadata};
          ctx.users.push({message: user, parent: ctx.parent});
          emit(ctx, user, 'pending', 'network', ctx.parent, true);
          ctx.parent = user.id;
        });
      } catch (_) { notice('提问格式未识别，请核对已保存原文。'); }
    }
    // unauth-mweb (logged-out) posts urlencoded forms: prompt=...&conversationState=...
    function parseForm(text) {
      if (ctx.closed || !text || text.length > MAX_JSON) return;
      var fields = {};
      String(text).split('&').forEach(function (pair) {
        var i = pair.indexOf('=');
        if (i <= 0) return;
        try { fields[decodeURIComponent(pair.slice(0, i).replace(/\+/g, '%20'))] =
          decodeURIComponent(pair.slice(i + 1).replace(/\+/g, '%20')); } catch (_) {}
      });
      var prompt = fields.prompt;
      if (!prompt || !prompt.trim()) { notice('未登录提问内容未能识别，请核对已保存原文。'); return; }
      var state = {};
      try { state = JSON.parse(fields.conversationState || '{}'); } catch (_) {}
      var initialId = ctx.cid;
      var serverCid = state.conversationId || state.conversation_id || '';
      if (serverCid) { bind(ctx, serverCid); latest[ctx.cid] = ctx.token; }
      else if (activeId === initialId) activate(ctx.cid);
      var user = {id: uid('u:'), author: {role: 'user'}, content: {parts: [prompt]}};
      ctx.users.push({message: user, parent: ctx.parent});
      emit(ctx, user, 'pending', 'network', ctx.parent, true);
    }
    var body = init && init.body;
    var ct = String(init && init.headers && (init.headers['Content-Type'] || init.headers['content-type']) || '');
    if (typeof body === 'string') {
      if (ct.indexOf('application/x-www-form-urlencoded') >= 0) parseForm(body);
      else parse(body);
      return Promise.resolve();
    }
    if (init && typeof URLSearchParams !== 'undefined' && body && body.constructor && body.constructor.name === 'URLSearchParams') {
      try { parseForm(body.toString()); return Promise.resolve(); } catch (_) {}
    }
    if (!body && input && typeof input.clone === 'function')
      return input.clone().text().then(parse).catch(function () { notice('未能读取本次提问。'); });
    return Promise.resolve();
  }
  function usersStatus(ctx, status) {
    ctx.users.forEach(function (u) {
      emit(ctx, u.message, status, 'network', u.parent, latest[ctx.cid] === ctx.token && !Object.keys(ctx.answers).length);
    });
  }
  function expandDelta(ctx, ev) {
    // ChatGPT-style snapshots / append / set / patch. Reject unknown operations and unsafe paths.
    // An operation may omit p/o and inherit them from the preceding delta in this response only.
    if (ev.v && ev.v.message && !ev.p && (!ev.o || ev.o === 'add' || ev.o === 'replace')) {
      ctx.delta = JSON.parse(JSON.stringify(ev.v)); ctx.deltaPath = ''; ctx.deltaOp = ev.o || 'add';
      return ctx.delta;
    }
    if (!Object.prototype.hasOwnProperty.call(ev, 'v') && !ev.o) return null;
    function apply(op) {
      if (!op || typeof op !== 'object') throw new Error('delta');
      if (op.o === 'patch') {
        if (!Array.isArray(op.v)) throw new Error('patch');
        op.v.forEach(apply); return;
      }
      var kind = op.o || ctx.deltaOp, pointer = op.p !== undefined ? op.p : ctx.deltaPath;
      if (!['add', 'replace', 'set', 'append', 'remove'].includes(kind) || typeof pointer !== 'string') throw new Error('operation');
      ctx.deltaOp = kind; ctx.deltaPath = pointer;
      if (pointer === '') {
        if (!['add', 'replace', 'set'].includes(kind) || !op.v || typeof op.v !== 'object') throw new Error('root');
        ctx.delta = JSON.parse(JSON.stringify(op.v)); return;
      }
      if (pointer.charAt(0) !== '/' || !ctx.delta) throw new Error('path');
      var keys = pointer.slice(1).split('/').map(function (key) { return key.replace(/~1/g, '/').replace(/~0/g, '~'); });
      if (keys.some(function (k) { return ['__proto__', 'prototype', 'constructor'].includes(k); })) throw new Error('unsafe path');
      var obj = ctx.delta;
      for (var i = 0; i < keys.length - 1; i++) {
        if (!obj || !Object.prototype.hasOwnProperty.call(obj, keys[i])) throw new Error('missing parent');
        obj = obj[keys[i]];
      }
      if (!obj || typeof obj !== 'object') throw new Error('parent type');
      var key = keys[keys.length - 1];
      if (kind === 'append') {
        if (typeof obj[key] === 'string' && typeof op.v === 'string') obj[key] += op.v;
        else if (Array.isArray(obj[key]) && Array.isArray(op.v)) obj[key].push.apply(obj[key], op.v);
        else throw new Error('append type');
      } else if (kind === 'remove') {
        if (Array.isArray(obj)) {
          var index = Number(key);
          if (!Number.isInteger(index) || index < 0 || index >= obj.length) throw new Error('array index');
          obj.splice(index, 1);
        } else delete obj[key];
      } else if (Array.isArray(obj) && kind === 'add') {
        var n = key === '-' ? obj.length : Number(key);
        if (!Number.isInteger(n) || n < 0 || n > obj.length) throw new Error('array index');
        obj.splice(n, 0, op.v);
      } else obj[key] = op.v;
    }
    try { apply(ev); return ctx.delta && ctx.delta.message ? ctx.delta : null; }
    catch (_) { ctx.unsupported = true; return null; }
  }
  function event(ctx, ev) {
    if (!ev || typeof ev !== 'object') return; // Encoding/version markers are not messages.
    if (ev.conversation_id) bind(ctx, ev.conversation_id);
    var payload = ev.message ? ev : expandDelta(ctx, ev);
    if (!payload) { ctx.unknown++; return; }
    if (payload.conversation_id) bind(ctx, payload.conversation_id);
    var m = payload.message;
    if (!m || !m.id || !m.author) return;
    if (m.author.role === 'user') {
      var u = ctx.users.find(function (x) { return x.message.id === m.id; });
      emit(ctx, m, 'complete', 'network', u ? u.parent : '', false);
      return;
    }
    if (m.author.role !== 'assistant' || m.channel === 'analysis') return;
    if (m.metadata && m.metadata.is_visually_hidden_from_conversation) return;
    // Thinking/reasoning message frames are valid protocol events, not
    // "unsupported" — they just carry no text (emit() skips them). Only an
    // unparsable data frame may degrade the batch to partial.
    var old = ctx.answers[m.id];
    var final = m.status === 'finished_successfully' || m.end_turn === true;
    var item = ctx.answers[m.id] = {message: m, parent: m.parent_id || (old && old.parent) || ctx.parent,
      status: final ? 'complete' : 'streaming', sentAt: old ? old.sentAt : 0};
    if (final || !old || Date.now() - item.sentAt >= 400) publish(ctx, item, item.status);
  }
  async function observeStream(resp, ctx) {
    var reader = resp.body.getReader(), decoder = new TextDecoder(), buffer = '', data = [];
    ctx.cancelObserver = function () { reader.cancel().catch(function () {}); };
    var bytes = 0, ended = false, timedOut = false;
    var timer = setTimeout(function () {
      timedOut = true; reader.cancel().catch(function () {});
    }, 240000);
    function dispatch() {
      if (!data.length) return;
      var text = data.join('\n'); data = [];
      if (text.trim() === '[DONE]') { ended = true; flush(ctx, ctx.unsupported ? 'partial' : 'complete'); return; }
      try { event(ctx, JSON.parse(text)); }
      catch (_) { ctx.unknown++; ctx.unsupported = true; }
    }
    function line(text) {
      if (!text) { dispatch(); return; }
      if (text.indexOf('data:') === 0) {
        var value = text.slice(5);
        data.push(value.charAt(0) === ' ' ? value.slice(1) : value);
      }
    }
    function feed(text, eof) {
      buffer += text;
      var i;
      while ((i = buffer.indexOf('\n')) >= 0) {
        line(buffer.slice(0, i).replace(/\r$/, '')); buffer = buffer.slice(i + 1);
      }
      if (eof && buffer) { line(buffer.replace(/\r$/, '')); buffer = ''; }
      if (eof) dispatch();
    }
    try {
      while (!ended) {
        var r = await reader.read();
        if (r.done) { feed(decoder.decode(), true); break; }
        bytes += r.value.byteLength;
        if (bytes > MAX_STREAM) throw new Error('limit');
        feed(decoder.decode(r.value, {stream: true}), false);
      }
      // EOF without DONE/finished_successfully is conservatively incomplete.
      flush(ctx, ctx.unsupported ? 'partial' : ended ? 'complete' : undefined);
      Object.keys(ctx.answers).forEach(function (id) {
        var item = ctx.answers[id];
        if (!ended && item.status !== 'complete') publish(ctx, item, 'partial');
      });
      if (timedOut || ctx.unsupported || !Object.keys(ctx.answers).length) {
        notice(timedOut ? '采集超时，已保存部分内容。' : '回复协议未识别，请使用页面补采集并核对原文。');
        captureDom();
      }
    } catch (_) {
      flush(ctx, 'partial'); notice('回复采集中断，已保存收到的内容，可重新加载会话补采集。');
    } finally {
      ctx.cancelObserver = null;
      clearTimeout(timer);
      reader.cancel().catch(function () {});
      reader.releaseLock();
    }
  }
  async function readJson(resp) {
    if (!resp.body) return null;
    var reader = resp.body.getReader(), decoder = new TextDecoder(), text = '', bytes = 0;
    var timer = setTimeout(function () { reader.cancel().catch(function () {}); }, 30000);
    try {
      while (true) {
        var r = await reader.read();
        if (r.done) break;
        bytes += r.value.byteLength;
        if (bytes > MAX_JSON) throw new Error('history limit');
        text += decoder.decode(r.value, {stream: true});
      }
      return JSON.parse(text + decoder.decode());
    } finally { clearTimeout(timer); reader.cancel().catch(function () {}); reader.releaseLock(); }
  }
  async function captureHistory(j, cid, requestContext) {
    if (!j || !j.mapping) return false;
    var map = j.mapping, ids = Object.keys(map);
    if (ids.length > 5000) { notice('历史会话超过采集上限，请分段导入。'); return false; }
    var ctx = {cid: cid, time: Date.now()};
    function visible(nodeId) {
      var visited = Object.create(null);
      while (nodeId && map[nodeId] && !visited[nodeId]) {
        visited[nodeId] = true;
        var node = map[nodeId], m = node.message;
        if (m && m.author && /^(user|assistant)$/.test(m.author.role) &&
            m.channel !== 'analysis' && !(m.metadata && m.metadata.is_visually_hidden_from_conversation))
          return m.id || nodeId;
        nodeId = node.parent;
      }
      return '';
    }
    // Keep every branch. The active leaf determines which branch is summarized.
    for (var index = 0; index < ids.length; index++) {
      if (index && index % 20 === 0) await new Promise(function (resolve) { setTimeout(resolve, 220); });
      // A delayed history response must not replace a reply generated after this GET began.
      if (requestContext.closed) return true;
      if (latest[cid] !== requestContext.historyRequest) { notice('历史加载期间产生了新回复，已跳过旧快照；可重新加载补齐历史。'); return true; }
      var id = ids[index], node = map[id], m = node.message;
      if (!m) continue;
      if (!m.id) m.id = id;
      var status = m.author && m.author.role === 'user' || m.status === 'finished_successfully' ? 'complete' : 'partial';
      if (emit(ctx, m, status, 'history', visible(node.parent), false)) networkMessages++;
    }
    send({type: 'conversation', conversationId: cid, title: j.title || '',
      activeLeaf: visible(j.current_node), coverage: 'history', url: 'https://chatgpt.com/c/' + encodeURIComponent(cid)});
    return true;
  }
  async function observe(resp, ctx, kind, cid) {
    if (ctx.closed) return;
    var ct = resp.headers.get('content-type') || '';
    if (!resp.ok) { usersStatus(ctx, 'failed'); return; }
    usersStatus(ctx, 'complete');
    if (ct.indexOf('event-stream') >= 0 && resp.body) return observeStream(resp, ctx);
    if (ct.indexOf('json') >= 0) {
      var j = await readJson(resp);
      if (kind === 'history') { if (!await captureHistory(j, cid, ctx)) notice('历史会话格式未识别。'); }
      else if (kind === 'title' && j && j.title)
        send({type: 'conversation', conversationId: cid || ctx.cid, title: j.title});
      else if (kind === 'send') {
        if (j && j.message) {
          event(ctx, j);
          Object.keys(ctx.answers).forEach(function (id) {
            var item = ctx.answers[id];
            publish(ctx, item, item.status === 'complete' ? 'complete' : 'partial');
          });
        }
        else notice('本次回复不是已支持的流格式，请核对原文或使用页面补采集。');
      }
      return;
    }
    // unauth-mweb HTML-partial streams: strip tags, keep the visible text as a
    // single assistant reply. Best-effort; the raw stream stays on the page.
    if (ct.indexOf('html') >= 0 && resp.body) return observeHtmlStream(resp, ctx);
  }
  async function observeHtmlStream(resp, ctx) {
    var reader = resp.clone().body.getReader(), decoder = new TextDecoder(), full = '';
    try {
      while (true) {
        var r = await reader.read();
        if (r.done) break;
        full += decoder.decode(r.value, {stream: true});
        if (full.length > MAX_TEXT * 2) { reader.cancel().catch(function () {}); break; }
      }
    } catch (_) {}
    var text = full
      .replace(/<script[\s\S]*?<\/script>/gi, ' ')
      .replace(/<style[\s\S]*?<\/style>/gi, ' ')
      .replace(/<[^>]+>/g, ' ')
      .replace(/&nbsp;/g, ' ').replace(/&amp;/g, '&').replace(/&lt;/g, '<').replace(/&gt;/g, '>')
      .replace(/[ \t]+/g, ' ').trim();
    if (!text) return;
    var user = ctx.users.length ? ctx.users[ctx.users.length - 1] : null;
    var parentId = user ? user.message.id : '';
    var m = {id: 'html:' + ctx.token, author: {role: 'assistant'}, content: {content_type: 'text', parts: [text.slice(0, MAX_TEXT)]},
      status: 'complete'};
    emit(ctx, m, 'complete', 'network', parentId, true);
  }
  var originalFetch = window.fetch.bind(window);
  window.fetch = function (input, init) {
    var url;
    try { url = new URL(typeof input === 'string' ? input : input.url || String(input), currentUrl()); }
    catch (_) { return originalFetch(input, init); }
    if (url.origin !== 'https://chatgpt.com') return originalFetch(input, init);
    // Diagnostic log: every same-origin POST/GET that might be a conversation call.
    // Surfaced as 'notice'-level events only when debug logging is on (see below);
    // keep it cheap — one send per request, bounded string.
    var method0 = String(init && init.method || input && input.method || 'GET').toUpperCase();
    if (url.pathname.indexOf('/conversation') >= 0 || url.pathname.indexOf('backend') >= 0)
      send({type: '__diag', path: url.pathname.slice(0, 120), method: method0});
    // Logged-out anonymous chats use /unauth-mweb/*, logged-in use /backend-api/*.
    // Two path shapes exist: /conversation/<id>/title (logged-in) and
    // /conversation/<action> (unauth-mweb: updates/prepare). Try id-shape first,
    // then action-shape, so neither swallows the other.
    var m1 = url.pathname.match(/^\/(backend-api|backend-anon|unauth-mweb)\/(?:f\/)?conversation(?:\/([^/]+))?(?:\/(title|updates|prepare))?\/?$/);
    var m2 = url.pathname.match(/^\/(backend-api|backend-anon|unauth-mweb)\/(?:f\/)?conversation\/(title|updates|prepare)\/?$/);
    var match = m2 || m1; // action-shape wins: /updates must not be read as a conversation id
    if (!match) return originalFetch(input, init);
    // Normalize: m1=[backend, id, action]; m2=[backend, action].
    var action = m2 ? m2[2] : (m1 ? m1[3] : '') || '';
    var convId = m2 ? '' : (m1 ? m1[2] : '') || '';
    var method = String(init && init.method || input && input.method || 'GET').toUpperCase();
    var kind = action === 'title' ? 'title' : action === 'updates' ? 'send'
        : method === 'GET' && convId ? 'history' : method === 'POST' && !convId ? 'send' : '';
    if (action === 'prepare') return originalFetch(input, init); // handshake, no content
    if (!kind) return originalFetch(input, init);
    var ctx = {cid: activeId || localId, token: uid('req:'), users: [], answers: Object.create(null),
      parent: '', time: Date.now(), unknown: 0, historyRequest: latest[convId || '']};
    contexts.push(ctx);
    var prepared = kind === 'send' ? prepare(input, init, ctx) : Promise.resolve();
    function finished() {
      if (kind === 'send') send({type: 'request', conversationId: ctx.cid, requestId: ctx.token, state: 'end'});
      var i = contexts.indexOf(ctx); if (i >= 0) contexts.splice(i, 1);
    }
    if (kind === 'send') prepared.then(function () {
      if (!ctx.closed) send({type: 'request', conversationId: ctx.cid, requestId: ctx.token, state: 'start'});
    });
    var fetched;
    try { fetched = originalFetch(input, init); }
    catch (e) { prepared.then(function () { usersStatus(ctx, 'failed'); finished(); }); throw e; }
    return fetched.then(function (resp) {
      var copy;
      try { copy = resp.clone(); }
      catch (_) {
        notice('无法复制响应，网页仍可使用；请核对采集结果。');
        prepared.then(function () { usersStatus(ctx, 'partial'); finished(); });
        return resp;
      }
      prepared.then(function () { return observe(copy, ctx, kind, convId || ''); })
        .catch(function () { flush(ctx, 'partial'); notice('采集未完成，请核对已保存原文。'); })
        .finally(function () { if (copy.body && !copy.body.locked) copy.body.cancel().catch(function () {}); finished(); });
      return resp;
    }, function (error) {
      prepared.then(function () { usersStatus(ctx, 'failed'); finished(); });
      throw error;
    });
  };

  function captureDom() {
    var nodes = window.document.querySelectorAll('[data-message-author-role]'), count = 0, parent = '';
    var cid = routeId() || activeId || localId;
    Array.prototype.forEach.call(nodes, function (node) {
      var role = node.getAttribute('data-message-author-role');
      if (!/^(user|assistant)$/.test(role)) return;
      var holder = node.closest('[data-message-id]');
      var id = node.getAttribute('data-message-id') || holder && holder.getAttribute('data-message-id');
      // No stable ID: do not invent identities that would duplicate network messages.
      if (!id) return;
      var body = node.querySelector('.markdown') || node;
      var text = String(body.innerText || '').trim();
      if (!text) return;
      var m = {id: id, author: {role: role}, content: {parts: [text]}};
      if (emit({cid: cid, time: Date.now()}, m, 'partial', 'dom', parent, false)) count++;
      parent = id;
    });
    if (count) {
      send({type: 'conversation', conversationId: cid, activeLeaf: parent, coverage: 'dom', url: currentUrl()});
      notice('已补采集页面中有稳定 ID 的可见消息；附件、折叠内容和未加载历史可能缺失。');
    } else notice('页面暂无可补采集的消息；当前页面结构可能不受支持。');
  }
  window.__chatnotes = {
    captureDom: captureDom,
    flush: function () { contexts.forEach(function (ctx) { flush(ctx); }); }
  };

  // ---- XHR diagnostics: unauth-mweb (logged-out frontend) sends its message
  // stream via XMLHttpRequest, not fetch. Log every XHR to see the real shape.
  var XHR = window.XMLHttpRequest;
  if (XHR && XHR.prototype) {
    var open0 = XHR.prototype.open, send0 = XHR.prototype.send;
    XHR.prototype.open = function (method, url) {
      this.__cn_method = String(method || 'GET').toUpperCase();
      this.__cn_url = String(url || '');
      return open0.apply(this, arguments);
    };
    XHR.prototype.send = function () {
      try {
        var u = new URL(this.__cn_url, currentUrl());
        if (u.origin === 'https://chatgpt.com')
          send({type: '__diag', path: u.pathname.slice(0, 120) + u.search.slice(0, 60), method: this.__cn_method + '/xhr'});
      } catch (_) {}
      return send0.apply(this, arguments);
    };
  }

  window.addEventListener('pagehide', function () {
    contexts.forEach(function (ctx) {
      flush(ctx, 'partial');
      usersStatus(ctx, 'partial');
      ctx.closed = true;
      if (ctx.cancelObserver) ctx.cancelObserver();
      send({type: 'request', conversationId: ctx.cid, requestId: ctx.token, state: 'end'});
    });
  });
  send({type: 'ready'});
})();
