# ChatNotes 修复版

基于用户提供的最新版 `ChatNotes-src.zip` 修复，应用版本 `0.2.0`（versionCode 2），包名继续为 `com.willam.chatnotes`。输入附件实际为 TAR，本交付包为标准 ZIP。

当前支持在 WebView 中访问 ChatGPT，采集受支持的文字对话，保存原文，再调用用户配置的 OpenAI 兼容接口整理为 Markdown。其他 AI 网站需要单独适配，不能自动套用 ChatGPT 协议。

本次只修复网页行为、数据保存和原有功能缺陷。FTS、语义检索和知识库问答没有实现；方案见单独交付的设计和提示词文档。

## 构建与测试

使用 JDK 17、Android SDK Platform 34 和 Build Tools 34.0.0。在 Android Studio 中打开本目录，同步依赖；SDK 路径由 IDE 生成在本机 `local.properties`，不随源码分发。

macOS / Linux：

```bash
chmod +x gradlew
./gradlew testDebugUnitTest assembleDebug
```

Windows：

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug
```

APK：`app/build/outputs/apk/debug/app-debug.apk`。连接测试设备后运行 `./gradlew connectedDebugAndroidTest`。

不依赖 Android 设备的采集与存储回归：

```bash
node --test tests/interceptor.test.cjs
java tests/RunJavaTests.java
```

需要支持原生 fetch / Response / ReadableStream 的 Node.js（本次使用 Node 24），Java 存储检查使用 JDK 17。实际执行范围、结果及未执行项目见 `VALIDATION.md`。

## 升级

保留原应用数据时，使用与已安装版本相同的签名构建，再覆盖安装：

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

不同签名不能直接覆盖；应回到原签名环境构建。不要通过卸载旧应用来绕过签名错误，卸载会删除私有目录内的数据。原 `filesDir/notes` Markdown 保留并继续读取；新采集的原文保存在 SQLite。旧版从未保存的原始对话无法凭摘要自动恢复，可以重新打开原平台历史进行采集。

首次启动会尝试将旧 API Key 迁移到 Android Keystore 加密存储；迁移成功才移除明文值。只清理旧诊断文件 `intercept_log.txt`，不删除笔记或登录 Cookie。自动备份排除原文、配置及 WebView 登录数据；需要备份时主动导出原文与笔记。

## 使用

1. “笔记”页右上角设置菜单 → “LLM 接口设置”：填写 HTTPS Base URL、API Key、模型 ID。Base URL 应是 `/chat/completions` 之前的接口前缀。
2. 在“对话”页登录 ChatGPT。当前 WebView 使用实际内核的默认 UA；部分登录服务可能限制嵌入式浏览器，需在设备上验证，不绕过服务端认证或证书校验。
3. 顶部显示已保存的消息数。点击可查看已保存会话及原文；附件、未知协议、页面补采集或断流会提示可能不完整。
4. 回复完成后点右上角关闭图标，确认要发送到的整理服务。整理任务可离开页面后继续；后台实际调度时间由 Android 决定。
5. 新增消息后可再次归档；相同快照重复点击不会重复生成。失败任务从设置菜单“整理任务与重试”继续，已完成分段及草稿会复用。
6. 设置菜单可补采集当前网页、查看原文和导出 ZIP。导出含聊天与笔记，请自行选择合适的保存位置；不包含接口凭据和登录 Cookie。
7. 原有标题/全文搜索仍采用文本匹配，已移到后台并防抖；不是语义问答。

## 主要文件

| 文件 | 作用 |
|---|---|
| `MainActivity.kt` | 安全网页桥接、原文查看、整理入口、设置和原有笔记界面 |
| `assets/interceptor.js` | 有界响应副本观察、已支持的 SSE 增量/完整格式、历史及页面补采集 |
| `ChatStore.kt` / `Models.kt` | 会话、消息分支、临时 ID、快照、整理任务与检查点 |
| `SummaryWorker.kt` / `LlmClient.kt` | 后台整理、结果验证、分块恢复、有界 HTTP 调用 |
| `NoteFiles.java` / `NotesRepo.kt` | 文件路径校验、原子保存、旧笔记浏览与文本匹配 |
| `ConfigStore.kt` | HTTPS 配置验证、密钥加密和明文迁移 |

## 采集边界

- 保留原版已修好的 `Response.clone()` 和 `/backend-api/f/conversation` 支持；不替换 Worker、WebSocket、EventSource 或 Beacon，不记录原始网络日志。
- 自动观察主页面受支持的 fetch；不会宣称已覆盖 Worker 内部网络、WebSocket 专有协议、所有第三方平台或隐藏历史。协议不匹配时请核对原文并补采集。
- DOM 补采集只读取有稳定消息 ID 的已加载文字；不凭空生成 ID，不下载附件，不读取隐藏分析通道。
- 单条文字约 18 万字符、旁路流 8 MiB / 4 分钟、历史 JSON 4 MiB / 30 秒、历史节点 5000 的边界均可能导致部分采集；旁路停止不会取消网页原始响应。超出限制会显示不完整提示。
- 整理输入上限 25 万字符，超长内容需导出后分段处理。服务端收到了请求但客户端未保存响应时，重试仍可能重复计费，不能保证跨外部 API 的 exactly-once。
- 原文数据库位于应用私有目录，但没有额外的数据库全文加密；Keystore 保护的是 API Key。卸载、清除应用数据或设备损坏仍可能导致本地内容丢失。
- 渲染进程退出后提供恢复入口，保留已落盘内容，不自动重发问题。尚未提交的输入和未收到的回复无法由本应用恢复。

修复明细见 `FIXES.md`；实测边界与设备清单见 `VALIDATION.md`。
