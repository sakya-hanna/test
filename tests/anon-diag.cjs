const fs = require('fs'), vm = require('vm');
const source = fs.readFileSync('app/src/main/assets/interceptor.js', 'utf8');
const sent = [];
const form = 'prompt=' + encodeURIComponent('未登录的问题') +
  '&conversationState=' + encodeURIComponent(JSON.stringify({conversationId:'anon-9',parentMessageId:'client-created-root'}));
const html = '<div>你好，这是<b>回复正文</b>。</div>';
const window = {
  fetch: async () => new Response(html, {headers:{'content-type':'text/vnd.openai.web-mobile-partial+html'}}),
  location: {href:'https://chatgpt.com/'},
  crypto: {randomUUID: require('crypto').randomUUID},
  history: {pushState(){},replaceState(){}},
  document: {querySelectorAll(){return[];}},
  addEventListener(){}, chatnotesProxy: {postMessage(s){sent.push(JSON.parse(s));}}
};
window.top = window;
vm.runInNewContext(source, {window, URL, URLSearchParams, TextDecoder, setTimeout, clearTimeout, setInterval:()=>0, clearInterval(){}, Promise, Date, console});
window.fetch('/unauth-mweb/conversation/updates', {
  method:'POST', headers:{'Content-Type':'application/x-www-form-urlencoded'}, body: form
}).then(r=>r.text()).then(async()=>{
  for (let i=0;i<10;i++) await new Promise(r=>setImmediate(r));
  console.log(JSON.stringify(sent.filter(e=>e.type!=='active'), null, 1));
});
