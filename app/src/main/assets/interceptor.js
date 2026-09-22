/* ChatNotes: observe a bounded CLONE, never consume/cancel the page's response. */
(function () {
  'use strict';
  if (window.top !== window || window.__chatnotes) return;
  var MAX_TEXT = 180000, MAX_STREAM = 8 * 1024 * 1024, MAX_JSON = 4 * 1024 * 1024;
  var latest = Object.create(null), contexts = [], localId = uid('local:'), activeId = '';
  var lastRoute = '', networkMessages = 0;
  // Platform detection: the capture script is shared; route/SSE/DOM details adapt per site.
  var PLATFORM = window.location.host === 'chat.deepseek.com' ? 'deepseek' : 'chatgpt';
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
    var path = new URL(currentUrl()).pathname;
    var m = PLATFORM === 'deepseek' ? path.match(/\/a\/chat\/s\/([a-zA-Z0-9_-]+)/)
                                    : path.match(/\/c\/([a-zA-Z0-9_-]+)/);
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
    var label = '';
    if (PLATFORM === 'deepseek') {
      // DeepSeek: document.title tracks the conversation topic once a message exists.
      var t = String(window.document.title || '').trim();
      if (t && !/^(deepseek|new chat|开启新对话|登录|log in)/i.test(t)) label = t;
    } else {
      // Sidebar link for the current conversation carries its title.
      var link = window.document.querySelector(
        'a[href*="/c/' + routeId() + '"][aria-label], a[data-testid*="conversation"][href*="' + routeId() + '"]');
      try { label = (link && link.getAttribute('aria-label') || '').replace(/^\s*(对话|Chat)\s*[:：]?\s*/i, '').trim(); } catch (_) {}
      if (!label) {
        // document.title is usually the conversation topic on chatgpt.com.
        var t2 = String(window.document.title || '').trim();
        if (t2 && !/^(chatgpt|new chat|新对话|登录|log in)/i.test(t2)) label = t2;
      }
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
    var body = init && init.body;
    if (typeof body === 'string') { parse(body); return Promise.resolve(); }
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
  // ---- DeepSeek: XHR + SSE patch stream ----
  // 真机 CDP 抓包实锤（2026-09）：请求经 XMLHttpRequest POST /api/v0/chat/completion，
  // 请求体平铺 {chat_session_id, parent_message_id, prompt, ...}（没有 messages 数组）。
  // 响应 application/x-ndjson 实为 SSE：行 "event: ready|update_session|close" + "data: {...}"。
  // data 帧四种形态：
  //   1) ready 帧 {request_message_id, response_message_id, model_type}
  //   2) 全量快照 {"v": {response: {...fragments[]}}}（仅流首）
  //   3) 隐式追加 {"v": "文本"}——向当前 RESPONSE 片段追加正文（流的主体形态）
  //   4) 显式补丁 {"p": 路径, "o": APPEND|SET|BATCH, "v": ...}；p 以 -1 结尾指向末尾片段；
  //      BATCH 的 v 是子补丁数组；o 省略时按 SET；无 p 无 o 只有 v 的 dict 不是补丁（忽略）
  // WebView 一定有 XMLHttpRequest；守卫只为测试沙箱与极端环境不炸整个采集脚本。
  if (typeof XMLHttpRequest !== 'undefined') {
    var originalOpen = XMLHttpRequest.prototype.open;
    var originalSend = XMLHttpRequest.prototype.send;
    XMLHttpRequest.prototype.open = function (method, url) {
      this.__cnUrl = String(url || '');
      this.__cnMethod = method;
      return originalOpen.apply(this, arguments);
    };
    XMLHttpRequest.prototype.send = function (body) {
      var xhr = this;
      var isCompletion = false;
      try {
        var u = new URL(xhr.__cnUrl, currentUrl());
        isCompletion = u.origin === 'https://chat.deepseek.com' &&
          /^\/api\/v0\/chat\/completion/.test(u.pathname) &&
          String(xhr.__cnMethod || 'POST').toUpperCase() !== 'GET';
      } catch (_) { isCompletion = false; }
      if (!isCompletion) return originalSend.apply(this, arguments);
      var dctx = {cid: activeId || localId, token: uid('req:'), users: [], answers: Object.create(null),
        parent: '', time: Date.now(), unknown: 0, ds: {list: [], global: '', seq: 0}};
      contexts.push(dctx);
      // 提问内容：请求体平铺 prompt 字段；chat_session_id 直接给真实会话 id。
      try {
        if (typeof body === 'string' && body) {
          var req = JSON.parse(body);
          if (req.chat_session_id) {
            var rid0 = String(req.chat_session_id);
            if (rid0 !== dctx.cid) {
              if (dctx.cid.indexOf('local:') === 0) bind(dctx, rid0);
              else dctx.cid = rid0;
            }
          }
          if (req.prompt && String(req.prompt).trim()) {
            var user = {id: 'dsreq-' + String(req.parent_message_id || ++dctx.ds.seq) + '-' + dctx.token.slice(8, 20),
              author: {role: 'user'}, content: {parts: [String(req.prompt).slice(0, MAX_TEXT)]}};
            dctx.users.push({message: user, parent: ''});
            emit(dctx, user, 'pending', 'network', '', true);
          }
        }
      } catch (_) { /* 请求体格式变化时静默，DOM 兜底 */ }
      send({type: 'request', conversationId: dctx.cid, requestId: dctx.token, state: 'start'});
      xhr.addEventListener('load', function () {
        try {
          var text = String(xhr.responseText || '');
          if (!text) { usersStatus(dctx, 'failed'); return; }
          usersStatus(dctx, 'complete');
          // 响应 MIME 标 x-ndjson，实为 SSE：只解析 data: 行（event: 行无载荷）。
          String(text).split('\n').forEach(function (row) {
            row = row.replace(/\r$/, '');
            if (row.indexOf('data:') !== 0) return;
            var payload = row.slice(5);
            if (payload.charAt(0) === ' ') payload = payload.slice(1);
            try { deepseekFrame(dctx, JSON.parse(payload)); }
            catch (_) { dctx.unknown++; }
          });
          var answers = dctx.ds.list.filter(function (fr) { return fr.type === 'RESPONSE' && fr.content.trim(); });
          if (answers.length) {
            answers.forEach(function (fr) {
              var st = dctx.answers[fr.id];
              if (!st) { st = dctx.answers[fr.id] = {id: fr.id, role: 'assistant', content: '', final: false, sentAt: 0, parent: ''}; }
              st.content = fr.content;
              st.final = String(fr.status || '').toUpperCase() === 'FINISHED' || dctx.ds.global === 'FINISHED';
              deepseekEmit(dctx, st);
            });
            flush(dctx, dctx.unsupported ? 'partial' : 'complete');
          } else {
            notice('回复未从网络捕获，已用页面补采集核对。');
            captureDom();
          }
        } catch (_) { flush(dctx, 'partial'); notice('回复采集出错，已保存收到的内容。'); }
      });
      xhr.addEventListener('error', function () { usersStatus(dctx, 'failed'); });
      var done = function () {
        send({type: 'request', conversationId: dctx.cid, requestId: dctx.token, state: 'end'});
        var i = contexts.indexOf(dctx); if (i >= 0) contexts.splice(i, 1);
      };
      xhr.addEventListener('loadend', done);
      return originalSend.apply(this, arguments);
    };
  }
  function deepseekText(obj) {
    // Accept string content or {text: "..."} / {content: "..."} shapes seen in fragments.
    if (!obj) return '';
    if (typeof obj === 'string') return obj;
    if (typeof obj.text === 'string') return obj.text;
    if (typeof obj.content === 'string') return obj.content;
    if (Array.isArray(obj)) return obj.map(deepseekText).join('');
    return '';
  }
  function deepseekEmit(ctx, item) {
    if (ctx.closed) return;
    var text = deepseekText(item.content);
    if (!text.trim()) return;
    var m = {id: item.id || uid('ds:'), author: {role: item.role || 'assistant'}, content: {parts: [text]}};
    if (emit(ctx, m, item.final ? 'complete' : 'streaming', 'network', item.parent || '', true)) networkMessages++;
  }
  function dsImport(f) {
    return {id: String(f.id), type: String(f.type || ''),
      content: typeof f.content === 'string' ? f.content : '', status: String(f.status || '')};
  }
  function dsLast(ctx) { return ctx.ds.list.length ? ctx.ds.list[ctx.ds.list.length - 1] : null; }
  function dsEnsure(ctx, id) {
    var found = null;
    for (var i = 0; i < ctx.ds.list.length; i++) {
      if (ctx.ds.list[i].id === String(id)) { found = ctx.ds.list[i]; break; }
    }
    if (!found) { found = dsImport({id: id}); ctx.ds.list.push(found); }
    return found;
  }
  function deepseekFrame(ctx, fr) {
    if (!fr || typeof fr !== 'object') { ctx.unknown++; return; }
    // ready 帧：本轮服务端消息编号（request=用户、response=回复）。
    if (fr.request_message_id !== undefined && fr.response_message_id !== undefined) return;
    // 隐式追加帧 {"v":"文本"}：向数组最后一个片段追加（不分类型——开思考/搜索时
    // 文本可能先进 THINK/SEARCH 片段，RESPONSE 建立后自然切过去）；空数组才自建。
    if (fr.hasOwnProperty('v') && !fr.hasOwnProperty('p') && !fr.hasOwnProperty('o')) {
      var v = fr.v;
      if (typeof v === 'string' && v) {
        var cur = dsLast(ctx);
        if (!cur) { cur = dsImport({id: 'implicit-' + ctx.token}); cur.type = 'RESPONSE'; ctx.ds.list.push(cur); }
        cur.content += v;
        return;
      }
      if (v && typeof v === 'object') {
        // 全量快照（仅流首）：response.fragments 是权威状态。
        var resp = v.response || null;
        if (resp && Array.isArray(resp.fragments)) {
          ctx.ds.list = resp.fragments.map(dsImport);
          return;
        }
      }
      ctx.unknown++;
      return;
    }
    if (fr.hasOwnProperty('p')) {
      // 显式补丁帧 {"p":路径,"o":APPEND|SET|BATCH,"v":...}；缺省 o=SET，BATCH 递归子补丁。
      function dsSub(prefix, op) {
        var childP = op && typeof op.p === 'string' ? op.p : '';
        var full = childP ? (prefix ? prefix + '/' + childP : childP) : prefix;
        if (op && op.o === 'BATCH' && Array.isArray(op.v)) {
          op.v.forEach(function (sub) { dsSub(full, sub); });
          return;
        }
        var kind = String(op && op.o || 'SET').toUpperCase();
        var val = op ? op.v : undefined;
        if (full === 'response/status') {
          if (typeof val === 'string') ctx.ds.global = val;
          return;
        }
        if (full === 'response/fragments' || full === 'fragments') {
          // 新片段追加：页面在此建立 RESPONSE/THINK 片段。
          if (kind === 'APPEND' && Array.isArray(val)) {
            val.forEach(function (f) { if (f && f.id !== undefined) ctx.ds.list.push(dsImport(f)); });
          }
          return;
        }
        var tgt = null, prop = '';
        var segs = full.split('/');
        var fi = segs.indexOf('fragments');
        if (fi >= 0 && segs[fi + 1] === '-1') {
          // -1 = 数组最后一个片段（真实流恒定用法；中间路径同样适用）。
          tgt = dsLast(ctx);
          if (!tgt) { tgt = dsImport({id: 'implicit-' + ctx.token}); tgt.type = 'RESPONSE'; ctx.ds.list.push(tgt); }
          prop = segs[fi + 2] || 'content';
        } else if (fi >= 0) {
          tgt = dsEnsure(ctx, segs[fi + 1]);
          prop = segs[fi + 2] || 'content';
        } else return; // 会话模式等与正文无关的路径
        if (prop === 'content') { if (kind === 'APPEND') { if (typeof val === 'string') tgt.content += val; } else if (typeof val === 'string') tgt.content = val; }
        else if (prop === 'status') { if (typeof val === 'string') tgt.status = val; }
        else if (prop === 'type') { if (typeof val === 'string') tgt.type = val; }
        // 其他属性（results/references/stage_id 等）与采集无关。
      }
      dsSub('', fr);
      return;
    }
    // update_session / click_behavior / 其他控制帧：忽略。
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
    }
  }
  var originalFetch = window.fetch.bind(window);
  window.fetch = function (input, init) {
    var url;
    try { url = new URL(typeof input === 'string' ? input : input.url || String(input), currentUrl()); }
    catch (_) { return originalFetch(input, init); }
    if (url.origin !== 'https://chatgpt.com') return originalFetch(input, init);
    // ChatGPT: logged-out anonymous chats use /backend-anon/*, logged-in use /backend-api/*.
    var match = url.pathname.match(/^\/(backend-api|backend-anon)\/(?:f\/)?conversation(?:\/([^/]+))?(?:\/(title))?\/?$/);
    if (!match) return originalFetch(input, init);
    var method = String(init && init.method || input && input.method || 'GET').toUpperCase();
    var kind = match[3] ? 'title' : method === 'GET' && match[2] ? 'history' : method === 'POST' && !match[2] ? 'send' : '';
    if (!kind) return originalFetch(input, init);
    var ctx = {cid: activeId || localId, token: uid('req:'), users: [], answers: Object.create(null),
      parent: '', time: Date.now(), unknown: 0, historyRequest: latest[match[2] || '']};
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
      prepared.then(function () { return observe(copy, ctx, kind, match[2] || ''); })
        .catch(function () { flush(ctx, 'partial'); notice('采集未完成，请核对已保存原文。'); })
        .finally(function () { if (copy.body && !copy.body.locked) copy.body.cancel().catch(function () {}); finished(); });
      return resp;
    }, function (error) {
      prepared.then(function () { usersStatus(ctx, 'failed'); finished(); });
      throw error;
    });
  };

  function captureDom() {
    var cid = routeId() || activeId || localId;
    var count = 0, parent = '';
    if (PLATFORM === 'deepseek') {
      // DeepSeek DOM: .ds-message containers; assistant messages contain a
      // .ds-markdown block, user messages do not. IDs are content-keyed so
      // repeated补采集 dedupes safely.
      var blocks = window.document.querySelectorAll('.ds-message');
      var seen = window.__chatnotesDsDom = window.__chatnotesDsDom || Object.create(null);
      Array.prototype.forEach.call(blocks, function (node) {
        var isUser = !node.querySelector('[class*="ds-markdown"]');
        var text = String(node.innerText || '').trim();
        if (!text || text.length < 2) return;
        var key = (isUser ? 'u' : 'a') + ':' + text.slice(0, 80);
        var id = seen[key];
        if (!id) { id = seen[key] = 'dsdom-' + (Object.keys(seen).length + 1) + '-' + cid.slice(0, 8); }
        var m = {id: id, author: {role: isUser ? 'user' : 'assistant'}, content: {parts: [text.slice(0, MAX_TEXT)]}};
        if (emit({cid: cid, time: Date.now()}, m, 'partial', 'dom', parent, false)) count++;
        parent = id;
      });
    } else {
      var nodes = window.document.querySelectorAll('[data-message-author-role]');
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
    }
    if (count) {
      send({type: 'conversation', conversationId: cid, activeLeaf: parent, coverage: 'dom', url: currentUrl()});
      notice('已补采集页面中有稳定 ID 的可见消息；附件、折叠内容和未加载历史可能缺失。');
    } else notice('页面暂无可补采集的消息；当前页面结构可能不受支持。');
  }
  window.__chatnotes = {
    captureDom: captureDom,
    flush: function () { contexts.forEach(function (ctx) { flush(ctx); }); }
  };
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
