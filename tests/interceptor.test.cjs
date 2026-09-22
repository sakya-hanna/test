const {test} = require('node:test');
const assert = require('node:assert/strict');
const vm = require('node:vm');
const fs = require('node:fs');
const path = require('node:path');
const source = fs.readFileSync(path.join(__dirname, '../app/src/main/assets/interceptor.js'), 'utf8');
const encoder = new TextEncoder();
const tick = () => new Promise(resolve => setImmediate(resolve));
async function settle() { for (let i=0;i<8;i++) await tick(); }
function message(text, id='a1', cid='c1', final=false) {
  return {conversation_id:cid, message:{id, author:{role:'assistant'},content:{content_type:'text',parts:[text]},
    status:final?'finished_successfully':'in_progress'}};
}
function sse(j, prefix='data: ') { return prefix + JSON.stringify(j) + '\n\n'; }
function request(messages=['你好'], cid='c1') {
  return {method:'POST',body:JSON.stringify({conversation_id:cid,parent_message_id:'root',messages:messages.map((text,i)=>({id:'u'+i,author:{role:'user'},content:{parts:[text]}}))})};
}
function install(fetch, href='https://chatgpt.com/c/c1', nodes=[]) {
  const sent=[]; const listeners={};
  const window={fetch,location:{href},crypto:{randomUUID:require('node:crypto').randomUUID},
    history:{pushState(){},replaceState(){}},document:{querySelectorAll(){return nodes;}},
    addEventListener(k,v){listeners[k]=v;},chatnotesProxy:{postMessage(s){sent.push(JSON.parse(s));}}};
  window.top=window;
  // Title-sync polls the DOM briefly; a real interval would keep node alive,
  // so the sandbox gets a self-clearing stub that never fires.
  const stubInterval = () => 0;
  vm.runInNewContext(source,{window,URL,TextDecoder,setTimeout,clearTimeout,
    setInterval:stubInterval,clearInterval(){},Promise,Date,console});
  return {window,sent,listeners};
}
function finals(sent,id='a1'){return sent.filter(e=>e.type==='message'&&e.msgId===id);}
const streamed = text=>new Response(text,{headers:{'content-type':'text/event-stream'}});

test('original Response remains readable and byte-for-byte unchanged',async()=>{
  const body=sse(message('你好，世界'))+'data: [DONE]\n\n';
  const {window,sent}=install(async()=>streamed(body));
  const result=await window.fetch('/backend-api/conversation',request());
  assert.equal(result.body.locked,false);
  assert.equal(await result.text(),body);
  await settle();
  assert.equal(finals(sent).at(-1).text,'你好，世界');
  assert.equal(finals(sent).at(-1).status,'complete');
  assert.equal(sent.filter(e=>e.type==='request').at(-1).state,'end');
});
test('interrupted streams preserve partial reply and preserve page error',async()=>{
  let controller;
  const stream=new ReadableStream({start(c){controller=c;}});
  const {window,sent}=install(async()=>new Response(stream,{headers:{'content-type':'text/event-stream'}}));
  const resp=await window.fetch('/backend-api/conversation',request());
  const pageRead=resp.text().then(()=>null,e=>e);
  controller.enqueue(encoder.encode(sse(message('已生成的内容'))));await settle();
  controller.error(new Error('fixture offline'));
  assert.ok(await pageRead); await settle();
  assert.equal(finals(sent).at(-1).text,'已生成的内容');
  assert.equal(finals(sent).at(-1).status,'partial');
});
test('request failure is explicitly failed, not a successful question',async()=>{
  const {window,sent}=install(async()=>{throw new Error('offline');});
  await assert.rejects(window.fetch('/backend-api/conversation',request()));await settle();
  assert.equal(finals(sent,'u0').at(-1).status,'failed');
  assert.equal(sent.filter(e=>e.type==='request').at(-1).state,'end');
});
test('HTTP errors preserve response and mark submitted questions failed',async()=>{
  const {window,sent}=install(async()=>new Response('denied',{status:403}));
  const resp=await window.fetch('/backend-api/conversation',request());
  assert.equal(await resp.text(),'denied');await settle();
  assert.equal(finals(sent,'u0').at(-1).status,'failed');
});
test('all request messages are retained with distinct IDs and parents',async()=>{
  const {window,sent}=install(async()=>streamed(sse(message('答复'))+'data: [DONE]\n\n'));
  await (await window.fetch('/backend-api/conversation',request(['第一条','第二条']))).text();await settle();
  assert.equal(finals(sent,'u0').at(-1).text,'第一条');
  assert.equal(finals(sent,'u1').at(-1).text,'第二条');
  assert.equal(finals(sent,'u1').at(-1).parentId,'u0');
});
test('history keeps all branches and declares selected leaf; JSON remains intact',async()=>{
  const user={id:'u',author:{role:'user'},content:{parts:['旧问题']}};
  const history={title:'历史标题',current_node:'a2',mapping:{root:{parent:null,message:null},
    u:{parent:'root',message:user},a1:{parent:'u',message:message('旧答案','a1','c1',true).message},
    a2:{parent:'u',message:message('新答案','a2','c1',true).message}}};
  const {window,sent}=install(async()=>Response.json(history));
  assert.deepEqual(await (await window.fetch('/backend-api/conversation/c1')).json(),history);await settle();
  assert.equal(sent.filter(e=>e.type==='message').length,3);
  assert.equal(finals(sent,'a2')[0].parentId,'u');
  assert.equal(sent.filter(e=>e.coverage==='history')[0].activeLeaf,'a2');
});
test('title route is reachable',async()=>{
  const {window,sent}=install(async()=>Response.json({title:'测试标题'}));
  await (await window.fetch('/backend-api/conversation/c1/title',{method:'POST'})).text();await settle();
  assert.equal(sent.filter(e=>e.type==='conversation').at(-1).title,'测试标题');
});
test('new conversations remap provisional IDs to server ID',async()=>{
  const {window,sent}=install(async()=>streamed(sse(message('新会话','a1','server-new'))+'data: [DONE]\n\n'),'https://chatgpt.com/');
  await (await window.fetch('/backend-api/conversation',request(['新问题'],''))).text();await settle();
  const remap=sent.find(e=>e.type==='remap');
  assert.ok(remap.from.startsWith('local:'));assert.equal(remap.to,'server-new');
  assert.equal(finals(sent).at(-1).conversationId,'server-new');
});
test('DONE publishes completion before EOF without cancelling page stream',async()=>{
  let controller;
  const {window,sent}=install(async()=>new Response(new ReadableStream({start(c){controller=c;}}),{headers:{'content-type':'text/event-stream'}}));
  const resp=await window.fetch('/backend-api/conversation',request());const text=resp.text();
  controller.enqueue(encoder.encode(sse(message('完成'))+'data: [DONE]\n\n'));await settle();
  assert.equal(finals(sent).at(-1).status,'complete');
  controller.enqueue(encoder.encode(': page still receives this\n\n'));controller.close();
  assert.match(await text,/page still receives this/);
});
test('UTF-8 split across bytes, CRLF and data without space are handled',async()=>{
  const body=(sse(message('中文🙂'),'data:')+'data:[DONE]\n\n').replaceAll('\n','\r\n');
  const bytes=encoder.encode(body);
  const {window,sent}=install(async()=>new Response(new ReadableStream({start(c){for(const b of bytes)c.enqueue(Uint8Array.of(b));c.close();}}),{headers:{'content-type':'text/event-stream'}}));
  assert.equal(await (await window.fetch('/backend-api/conversation',request())).text(),body);await settle();
  assert.equal(finals(sent).at(-1).text,'中文🙂');assert.equal(finals(sent).at(-1).status,'complete');
});
test('multimodal parts are identified without object coercion',async()=>{
  const {window,sent}=install(async()=>streamed('data: [DONE]\n\n'));
  const req=request();const j=JSON.parse(req.body);j.messages[0].content.parts.push({content_type:'image_asset_pointer'});req.body=JSON.stringify(j);
  await (await window.fetch('/backend-api/conversation',req)).text();await settle();
  const u=finals(sent,'u0').at(-1);assert.equal(u.attachments,true);assert.doesNotMatch(u.text,/object Object/);
});
test('other origins and unrelated paths are untouched',async()=>{
  const {window,sent}=install(async()=>streamed(sse(message('不应采集'))));
  await (await window.fetch('https://example.org/backend-api/conversation',request())).text();
  await (await window.fetch('/backend-api/conversations')).text();await settle();
  assert.equal(sent.filter(e=>e.type==='message').length,0);
});
test('Request objects preserve original request body and response',async()=>{
  const req=new Request('https://chatgpt.com/backend-api/conversation',request());let body;
  const {window,sent}=install(async input=>{body=await input.text();return streamed(sse(message('答'))+'data: [DONE]\n\n');});
  await (await window.fetch(req)).text();await settle();
  assert.equal(JSON.parse(body).messages[0].content.parts[0],'你好');assert.equal(finals(sent,'u0').at(-1).text,'你好');
});
test('EOF without explicit completion is conservatively partial',async()=>{
  const {window,sent}=install(async()=>streamed(sse(message('可能被截断'))));
  await (await window.fetch('/backend-api/conversation',request())).text();await settle();
  assert.equal(finals(sent).at(-1).status,'partial');
});
test('unsupported patch protocol never claims complete capture',async()=>{
  const body=sse(message('开头'))+sse({p:'/message/content/parts/0',o:'future-operation',v:'后半段'})+'data: [DONE]\n\n';
  const {window,sent}=install(async()=>streamed(body));
  await (await window.fetch('/backend-api/conversation',request())).text();await settle();
  assert.equal(finals(sent).at(-1).status,'partial');assert.ok(sent.some(e=>e.type==='notice'));
});

test('snapshot plus inherited delta append on f/conversation is reconstructed',async()=>{
  const snapshot=message('前半段');
  const body=sse({p:'',o:'add',v:snapshot})+sse({p:'/message/content/parts/0',o:'append',v:'后半段'})+sse({v:'结尾'})+'data: [DONE]\n\n';
  const {window,sent}=install(async()=>streamed(body));
  await (await window.fetch('/backend-api/f/conversation',request())).text();await settle();
  assert.equal(finals(sent).at(-1).text,'前半段后半段结尾');assert.equal(finals(sent).at(-1).status,'complete');
});
test('unsafe delta keys cannot pollute object prototypes',async()=>{
  const body=sse({p:'',o:'add',v:message('内容')})+sse({p:'/__proto__/polluted',o:'set',v:true})+'data: [DONE]\n\n';
  const {window,sent}=install(async()=>streamed(body));
  await (await window.fetch('/backend-api/conversation',request())).text();await settle();
  assert.equal(finals(sent).at(-1).status,'partial');assert.equal({}.polluted,undefined);
});

test('browser constructors and Worker options remain native after capture injection',()=>{
  const calls=[];
  function Worker(url,options){calls.push({url,options});}
  function EventSource(){}
  Object.assign(EventSource,{CONNECTING:0,OPEN:1,CLOSED:2});
  function WebSocket(){}
  const sendBeacon=()=>true;
  const window={fetch:async()=>new Response(''),Worker,EventSource,WebSocket,
    navigator:{sendBeacon},location:{href:'https://chatgpt.com/'},
    history:{pushState(){},replaceState(){}},document:{querySelectorAll(){return[];}},
    addEventListener(){},chatnotesProxy:{postMessage(){}}};
  window.top=window;
  vm.runInNewContext(source,{window,URL,TextDecoder,setTimeout,clearTimeout,
    setInterval:()=>0,clearInterval(){},Promise,Date});
  const options={type:'module',name:'parser',credentials:'include'};
  new window.Worker('module.js',options);
  assert.equal(calls[0].options,options);
  assert.equal(window.Worker,Worker);assert.equal(window.EventSource,EventSource);
  assert.equal(window.EventSource.OPEN,1);assert.equal(window.WebSocket,WebSocket);
  assert.equal(window.navigator.sendBeacon,sendBeacon);
});
test('malformed SSE cannot falsely mark a partial reply complete',async()=>{
  const body=sse(message('收到的开头'))+'data: {broken json}\n\ndata: [DONE]\n\n';
  const {window,sent}=install(async()=>streamed(body));
  await (await window.fetch('/backend-api/conversation',request())).text();await settle();
  assert.equal(finals(sent).at(-1).status,'partial');
});
test('delayed Request parsing remains associated with its originating conversation',async()=>{
  let release;
  const {window,sent}=install(async()=>streamed(sse(message('答','a1','c1'))+'data: [DONE]\n\n'));
  const input={url:'https://chatgpt.com/backend-api/conversation',method:'POST',clone(){return{text:()=>new Promise(r=>release=r)};}};
  const response=await window.fetch(input);
  window.location.href='https://chatgpt.com/c/c2';window.history.pushState({},'',window.location.href);
  release(request(['原会话的问题'],'').body);
  await response.text();await settle();
  assert.equal(finals(sent,'u0').at(-1).conversationId,'c1');
  assert.equal(sent.filter(e=>e.type==='active').at(-1).conversationId,'c2');
});
test('stale history does not overwrite a newer live answer or change its active branch',async()=>{
  let release;
  const {window,sent}=install(async(input)=>String(input).endsWith('/c1')?
    new Promise(r=>release=r):streamed(sse(message('新答案','a1','c1',true))+'data: [DONE]\n\n'));
  const old=window.fetch('/backend-api/conversation/c1');
  await (await window.fetch('/backend-api/conversation',request())).text();await settle();
  release(Response.json({title:'旧快照',current_node:'a1',mapping:{a1:{parent:null,message:message('旧答案','a1','c1',false).message}}}));
  await (await old).text();await settle();
  assert.equal(finals(sent).at(-1).text,'新答案');
  assert.equal(sent.filter(e=>e.coverage==='history').length,0);
});
test('non-stream JSON without completion marker is partial, never stuck streaming',async()=>{
  const {window,sent}=install(async()=>Response.json(message('片段')));
  await (await window.fetch('/backend-api/f/conversation',request())).text();await settle();
  assert.equal(finals(sent).at(-1).status,'partial');
  assert.equal(sent.filter(e=>e.type==='request').at(-1).state,'end');
});
test('navigation during generation saves partial text and releases the in-flight request',async()=>{
  let controller;
  const {window,sent,listeners}=install(async()=>new Response(new ReadableStream({start(c){controller=c;}}),{headers:{'content-type':'text/event-stream'}}));
  const response=await window.fetch('/backend-api/conversation',request());const read=response.text();
  controller.enqueue(encoder.encode(sse(message('离开前收到的内容'))));await settle();
  listeners.pagehide();await settle();
  assert.equal(finals(sent).at(-1).status,'partial');
  assert.equal(sent.filter(e=>e.type==='request').at(-1).state,'end');
  controller.enqueue(encoder.encode(sse(message('网页仍然可读取'))));controller.close();
  assert.match(await read,/网页仍然可读取/);await settle();
  assert.equal(finals(sent).at(-1).text,'离开前收到的内容');
});
test('navigation before asynchronous request parsing cannot leave a ghost request',async()=>{
  let release;
  const {window,sent,listeners}=install(async()=>streamed(sse(message('旧页面回复'))+'data: [DONE]\n\n'));
  const input={url:'https://chatgpt.com/backend-api/conversation',method:'POST',clone(){return{text:()=>new Promise(r=>release=r)};}};
  const response=await window.fetch(input);
  listeners.pagehide();release(request().body);
  await response.text();await settle();
  assert.equal(sent.filter(e=>e.type==='request').at(-1).state,'end');
  assert.equal(sent.filter(e=>e.type==='message').length,0);
});

test('anonymous logged-out backend (backend-anon) is captured too',async()=>{
  const body=sse(message('匿名回复内容','a9','anon1'))+'data: [DONE]\n\n';
  const {window,sent}=install(async()=>streamed(body));
  await (await window.fetch('/backend-anon/f/conversation',request(['未登录的问题'],'anon1'))).text();
  await settle();
  const msgs=sent.filter(e=>e.type==='message');
  assert.ok(msgs.some(m=>m.text==='未登录的问题'&&m.role==='user'));
  assert.ok(msgs.some(m=>m.text==='匿名回复内容'));
  // request body carries conversation_id, so messages land directly on that cid
  assert.ok(msgs.every(m=>m.conversationId==='anon1'));
});

test('thinking frames are not captured as attachment placeholders',async()=>{
  const thinking={conversation_id:'c1',message:{id:'t1',author:{role:'assistant'},
    content:{content_type:'thinking',parts:[]},status:'finished_successfully'}};
  const body=sse(thinking)+sse(message('正文内容','a1'))+'data: [DONE]\n\n';
  const {window,sent}=install(async()=>streamed(body));
  await (await window.fetch('/backend-api/conversation',request())).text();await settle();
  const msgs=sent.filter(e=>e.type==='message');
  // distinct assistant ids: only the real answer; the thinking frame never emits
  const ids=new Set(msgs.filter(m=>m.role==='assistant').map(m=>m.msgId));
  assert.equal(ids.size,1);assert.ok(ids.has('a1'));
  assert.ok(!msgs.some(m=>m.text.includes('附件或非文本内容未采集')));
  // clean stream -> complete, not partial
  assert.equal(finals(sent).at(-1).status,'complete');
});

// ---- DeepSeek: XHR + SSE patch stream（帧形状来自 2026-09 真机 CDP 抓包）----
function installDeepseek(fetch, href='https://chat.deepseek.com/a/chat/s/c1', nodes=[]) {
  const sent=[]; const listeners={};
  const window={fetch,location:{href},crypto:{randomUUID:require('node:crypto').randomUUID},
    history:{pushState(){},replaceState(){}},document:{querySelectorAll(){return nodes;}},
    addEventListener(k,v){listeners[k]=v;},chatnotesProxy:{postMessage(s){sent.push(JSON.parse(s));}}};
  window.top=window;
  function XMLHttpRequest(){}
  XMLHttpRequest.prototype.open=function(method,url){this.__cnUrl=String(url);this.__cnMethod=method;};
  XMLHttpRequest.prototype.addEventListener=function(k,v){(this.__cnL=this.__cnL||{})[k]=(this.__cnL[k]||[]).concat(v);};
  XMLHttpRequest.prototype.send=function(body){
    this.__cnBody=body;
    const self=this;
    // setImmediate 与 settle() 的轮询同一队列，触发时机确定（setTimeout 是宏任务，会竞态）
    setImmediate(()=>{
      self.responseText=self.__cnRsp||'';
      ((self.__cnL||{}).load||[]).forEach(f=>f());
      ((self.__cnL||{}).loadend||[]).forEach(f=>f());
    });
  };
  window.XMLHttpRequest=XMLHttpRequest;
  vm.runInNewContext(source,{window,XMLHttpRequest,URL,TextDecoder,setTimeout,clearTimeout,
    setInterval:()=>0,clearInterval(){},Promise,Date,console});
  return {window,sent,listeners};
}
function dsStream(frames){ return frames.map(f=>'data: '+JSON.stringify(f)+'\n\n').join(''); }
function dsFinals(sent){ return sent.filter(e=>e.type==='message'&&e.role==='assistant'); }
function dsRequest(prompt, sid='c1', parent=2){
  return JSON.stringify({chat_session_id:sid,parent_message_id:parent,model_type:null,prompt,
    ref_file_ids:[],thinking_enabled:false,search_enabled:true,action:null,preempt:false});
}
function dsSend(window,rsp,reqBody){
  const xhr=new window.XMLHttpRequest();
  xhr.__cnRsp=rsp;
  xhr.open('POST','https://chat.deepseek.com/api/v0/chat/completion');
  xhr.send(reqBody);
}

test('deepseek: prompt question and patch-stream reply captured from network',async()=>{
  const {window,sent}=installDeepseek(async()=>new Response(''));
  dsSend(window,dsStream([
    {request_message_id:5,response_message_id:6,model_type:'default'},
    {updated_at:1790044441.5},
    {v:{response:{message_id:6,role:'ASSISTANT',status:'WIP',fragments:[{id:2,type:'SEARCH',content:''}]}}},
    {p:'response/fragments/-1',o:'BATCH',v:[{p:'status',v:'FINISHED'},{p:'content',v:'搜索到 20 个网页'}]},
    {p:'response',o:'BATCH',v:[{p:'fragments',o:'APPEND',v:[{id:3,type:'RESPONSE',content:'搭建'}]},{p:'has_pending_fragment',o:'SET',v:false}]},
    {p:'response/fragments/-1/content',o:'APPEND',v:'AI 平台'},
    {v:'的步骤'},
    {p:'response/status',o:'SET',v:'FINISHED'}
  ]),dsRequest('什么是容器编排'));
  await settle();
  const msgs=sent.filter(e=>e.type==='message');
  const user=msgs.find(m=>m.role==='user');
  assert.ok(user);assert.equal(user.text,'什么是容器编排');
  assert.equal(user.source,'network');assert.equal(user.conversationId,'c1');
  // SEARCH 片段的元数据文本不得混进回答
  assert.ok(!msgs.some(m=>m.text.includes('搜索到')));
  assert.equal(dsFinals(sent).at(-1).text,'搭建AI 平台的步骤');
  assert.equal(dsFinals(sent).at(-1).source,'network');
  assert.equal(dsFinals(sent).at(-1).status,'complete');
  assert.equal(sent.filter(e=>e.type==='request').at(-1).state,'end');
});
test('deepseek: reply-less stream keeps the question and falls back to DOM notice',async()=>{
  const {window,sent}=installDeepseek(async()=>new Response(''));
  dsSend(window,'data: {"updated_at":1}\n\ndata: {"click_behavior":"none"}\n\n',dsRequest('这次没有回答'));
  await settle();
  const msgs=sent.filter(e=>e.type==='message');
  assert.ok(msgs.some(m=>m.role==='user'&&m.text==='这次没有回答'&&m.source==='network'));
  assert.equal(msgs.filter(m=>m.role==='assistant').length,0);
  assert.ok(sent.some(e=>e.type==='notice'));
});
test('deepseek: new conversation remaps local id via chat_session_id',async()=>{
  const {window,sent}=installDeepseek(async()=>new Response(''),'https://chat.deepseek.com/');
  dsSend(window,dsStream([
    {v:{response:{message_id:9,role:'ASSISTANT',status:'WIP',fragments:[]}}},
    {p:'response',o:'BATCH',v:[{p:'fragments',o:'APPEND',v:[{id:4,type:'RESPONSE',content:'新会话的回答'}]}]},
    {p:'response/fragments/-1/status',o:'SET',v:'FINISHED'}
  ]),dsRequest('新建对话提问','srv9'));
  await settle();
  const remap=sent.find(e=>e.type==='remap');
  assert.ok(remap);assert.ok(remap.from.startsWith('local:'));assert.equal(remap.to,'srv9');
  const msgs=sent.filter(e=>e.type==='message');
  assert.ok(msgs.length>0&&msgs.every(m=>m.conversationId==='srv9'));
  assert.equal(dsFinals(sent).at(-1).text,'新会话的回答');
  assert.equal(dsFinals(sent).at(-1).status,'complete');
});
test('deepseek: patches landing before any fragment still form the answer',async()=>{
  const {window,sent}=installDeepseek(async()=>new Response(''));
  dsSend(window,dsStream([
    {p:'response/fragments/-1/content',o:'APPEND',v:'直接开始'},
    {p:'response/status',o:'SET',v:'FINISHED'}
  ]),dsRequest('极端顺序'));
  await settle();
  assert.equal(dsFinals(sent).at(-1).text,'直接开始');
  assert.equal(dsFinals(sent).at(-1).status,'complete');
});
