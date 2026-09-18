# ChatNotes

WebView 加载 chatgpt.com,网络层被动拦截对话;关闭页面时调用你配置的 OpenAI 兼容 LLM 总结,按领域树归档为本地 Markdown 笔记。

## 构建

```
cd ChatNotes
set JAVA_HOME=D:\jdk-21.0.10\jdk-21.0.10\jdk-21.0.10
set ANDROID_HOME=C:\Users\willam\Android\Sdk
gradlew.bat assembleDebug
```

产物: app/build/outputs/apk/debug/app-debug.apk

## 安装到手机

```
adb install app/build/outputs/apk/debug/app-debug.apk
```

## 使用

1. 打开 App,首次在 chatgpt.com 登录(cookie 会保留)
2. 右上角齿轮配置 LLM:Base URL(如 https://open.bigmodel.cn/api/paas/v4)、API Key、模型名
3. 正常聊天,顶部绿字显示"已捕获 N 条消息"
4. 聊完点右上角 ✕ → 确认总结 → 笔记自动归档
5. "笔记"Tab:多级钻取(面包屑)、全文搜索、Markdown 渲染

## 架构

- InterceptorJs.kt — document-start 注入的 fetch 包装脚本(旁听请求体 + SSE 流,零额外请求)
- LlmClient.kt — OpenAI 兼容 /chat/completions,让 LLM 输出 {path, title, markdown} JSON
- NotesRepo.kt — filesDir/notes/ 下的目录树 = 领域树,.md = 笔记
- MainActivity.kt — 三屏:WebView / 领域树浏览 / 详情;设置对话框

安全设计:UA 伪装 Chrome、纯旁听零额外请求、document-start 注入仅限 chatgpt.com 域。

## 已知限制(下一步)

- DOM 抓取降级路径尚未实现(拦截失败时的兜底)
- 对话缓冲在内存中,App 被杀会丢
- 无新笔记角标/通知;分享链接导入模式未做
