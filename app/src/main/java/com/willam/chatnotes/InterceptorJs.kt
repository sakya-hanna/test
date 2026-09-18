package com.willam.chatnotes

/**
 * 拦截器注入脚本:由 androidx.webkit 的 document-start 注入,
 * 在页面任何脚本运行前执行,因此能抢在页面拿到 fetch 引用之前完成包装。
 * 原理:包装 window.fetch —— 页面自己调用 backend-api/conversation 时,
 * 我们旁听请求体(用户提问)与 SSE 响应流(GPT 回复),原样转发,零额外请求。
 */
object InterceptorJs {
    const val SRC = """
(function(){
  if (window.__chatnotes_hooked) return;
  window.__chatnotes_hooked = true;

  var send = function(obj){
    try { window.chatnotesProxy.postMessage(JSON.stringify(obj)); } catch(e){}
  };

  var origFetch = window.fetch.bind(window);

  window.fetch = function(input, init){
    var url = '';
    try {
      url = (typeof input === 'string') ? input
          : (input && input.url) ? input.url : '';
    } catch(e){}

    var isConv = url.indexOf('/backend-api/conversation') !== -1;
    var isTitle = url.indexOf('/title') !== -1;

    if (isConv) {
      // ---- 请求体:用户本轮提问 ----
      var parseBody = function(bodyText){
        if (!bodyText) return;
        try {
          var j = JSON.parse(bodyText);
          var parts = (j.messages && j.messages[0] && j.messages[0].content && j.messages[0].content.parts) || [];
          send({ type:'user_msg',
                 conversationId: j.conversation_id || '',
                 model: j.model || '',
                 text: parts.join('\n') });
        } catch(e){}
      };
      try {
        var b = init && init.body;
        if (typeof b === 'string') {
          parseBody(b);
        } else if (!b && input && typeof input.clone === 'function') {
          input.clone().text().then(parseBody).catch(function(){});
        }
      } catch(e){}

      // ---- 响应:SSE 流旁听 ----
      return origFetch(input, init).then(function(resp){
        try {
          var ct = resp.headers.get('content-type') || '';
          if (ct.indexOf('event-stream') !== -1 && resp.body) {
            var reader = resp.body.getReader();
            var dec = new TextDecoder();
            var buf = '';
            var tail = { conversationId:'', msgId:'', text:'' };

            var flush = function(){
              if (tail.msgId && tail.text) {
                send({ type:'assistant_msg', conversationId: tail.conversationId, msgId: tail.msgId, text: tail.text });
              }
              tail = { conversationId:'', msgId:'', text:'' };
            };

            var pump = function(){
              return reader.read().then(function(r){
                if (r.done) { flush(); return; }
                buf += dec.decode(r.value, { stream:true });
                var lines = buf.split('\n');
                buf = lines.pop();
                for (var i=0;i<lines.length;i++){
                  var line = lines[i];
                  if (line.indexOf('data: ') !== 0) continue;
                  var payload = line.slice(6).trim();
                  if (!payload || payload === '[DONE]') continue;
                  try {
                    var ev = JSON.parse(payload);
                    var m = ev.message;
                    if (!m || !m.id) continue;
                    var mid = m.id;
                    var cid = ev.conversation_id || tail.conversationId;
                    // 消息 id 切换 = 上一条 assistant 完整消息结束
                    if (tail.msgId && mid !== tail.msgId) flush();
                    if (m.author && m.author.role === 'assistant'
                        && m.content && m.content.content_type === 'text'
                        && m.content.parts) {
                      var t = m.content.parts.filter(function(p){ return typeof p === 'string'; }).join('\n');
                      if (t) { tail.msgId = mid; tail.conversationId = cid; tail.text = t; }
                    }
                  } catch(e){}
                }
                return pump();
              });
            };
            pump().catch(function(){});
          }
        } catch(e){}
        return resp;
      });
    }

    if (isTitle) {
      return origFetch(input, init).then(function(resp){
        try {
          resp.clone().json().then(function(j){
            if (j && j.title) send({ type:'title', conversationId: j.conversation_id || '', title: j.title });
          }).catch(function(){});
        } catch(e){}
        return resp;
      });
    }

    return origFetch(input, init);
  };
})();
"""
}
