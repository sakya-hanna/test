# 修复清单

基线为最新附件，SHA-256：`7e6e2f68735d2a6f23c50b58b4bfeacd64631fe005fe50b9729b9dd14a8ef195`。

输入版的 `resp.clone()` 与 `/backend-api/f/conversation` 已经修好，本版保留，不把它们算作输入版现存缺陷。

| 问题 | 原因与触发条件 | 修改位置 | 验证 |
|---|---|---|---|
| Worker 的 module/name/credentials 丢失 | 包装构造器只传一个参数 | `assets/interceptor.js` 不再包装构造器 | 输入版隔离复现，修复版原生对象/参数回归通过 |
| EventSource 状态常量丢失 | 替换构造器没保留静态字段 | 同上 | 输入版 OPEN 为 undefined，修复版回归通过 |
| 网页诊断造成主线程写盘与原文日志增长 | 每条 ws/es/beacon 等消息均在 UI appendText | `MainActivity`、`AppGraph` | 移除原始日志，旧文件后台清理；实际帧率改善待设备测量 |
| 会话串线、重复消息 | 全局 role/text 队列不使用会话和消息 ID | `ChatStore`、采集脚本 | JS 会话/临时 ID/异步请求测试通过，SQLite 仪器用例待设备执行 |
| 首次总结后不再整理新内容 | 全局 summarized 永久置真 | 快照指纹、持久任务 | JUnit 指纹回归通过，新增内容任务的 SQLite 用例已提供 |
| 退出丢原文、后台任务依赖 Activity | 内存队列 + 普通 Thread | `ChatStore`、`SummaryWorker` | 编译通过；进程中断/WorkManager 恢复待设备验证 |
| 同名笔记覆盖、目录越界、保存异常崩溃 | 标题作为文件唯一名、未校验分类、UI 未捕获写入异常 | `NoteFiles`、`NotesRepo`、Worker | 13 项实际生产存储检查通过 |
| 只读完整 SSE、断流丢尾部、历史忽略 | 固定单行解析，pump 错误静默 | `assets/interceptor.js` | 完整/增量、UTF-8、CRLF、断流、未知协议、历史分支均有回归 |
| 页面退出遗留“生成中”任务 | 新的持久请求记录需要随网页退出关闭 | 脚本 pagehide、ChatStore.closeCapture | 导航与异步请求交错回归通过；SQLite owner 隔离用例已提供 |
| 旧历史快照覆盖新回复 | 历史请求和实时回复异步到达 | 请求代次校验、消息来源优先级 | 延迟历史响应回归通过 |
| JavaScriptInterface 来源不受限 | 全 frame 桥接 + 任意主页面导航 | 精确来源、主 frame WebMessageListener | 静态检查与编译；第三方 frame 测试待设备执行 |
| Key 明文与默认备份 | 未加密 SharedPreferences，备份规则未正确接入 | `ConfigStore`、Manifest、备份规则 | 编译/XML 检查，旧 Key 迁移需设备验证 |
| LLM 响应异常、重定向、长输入 | 请求缺少边界和恢复状态 | `LlmClient`、`SummaryWorker` | JSON 校验/Unicode 分块 JUnit 通过；真实服务调用未执行 |
| 原有搜索卡 UI、返回行为不一致 | 每字遍历全库、返回逻辑未区分 Tab | 后台搜索/防抖/分批展示、Tab 返回路径 | 编译通过，交互性能需设备验证 |
| 渲染退出无法恢复 | 无退出处理，失效 WebView 仍被使用 | `MainActivity.onRenderProcessGone` | 编译通过，退出模拟与登录恢复需设备验证 |

没有新增 FTS、embedding、RAG 或改造成新的知识库产品。源码保留原目录与 Markdown；未来改造在单独交付的设计和提示词文档中。
