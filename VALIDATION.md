# 验证记录

以下只陈述实际运行的结果。测试使用合成对话，没有登录用户账号、调用真实模型服务或发送用户聊天。

## 已执行

| 检查 | 环境/范围 | 结果 |
|---|---|---|
| 最新输入版缺陷复现 | Node vm 执行原 `InterceptorJs.SRC`，注入模拟的原生构造器 | Worker options 被丢弃、EventSource.OPEN 变 undefined，两项复现 |
| 采集回归 | Node 24，执行实际 `assets/interceptor.js`；原生 Response/ReadableStream + 合成响应 | 24 项通过 |
| 文件存储 | JDK 17，编译并执行实际 `NoteFiles.java` | 13 项通过 |
| Kotlin/JVM 回归 | Kotlin 1.9.24 + JUnit 4.13.2 | 4 项通过 |
| 生产源码类型编译 | Kotlin 1.9.24、Java 17、Android API 34 android.jar、实际 AndroidX/Markwon 依赖 | 全部生产 Kotlin/Java 文件通过，仅 XML 回调未使用参数警告 |
| 归档与资源检查 | 标准 ZIP 内容、CRC、XML 语法、必需项目文件 | 通过（完整文件及 CRC 校验） |

共 41 项自动回归/存储检查通过，另外复现了输入版的两项浏览器构造器问题。

独立类型编译使用从现有 XML id/layout 生成的 R 常量，仅验证源码类型与依赖接口；**没有执行 AGP 资源合并、Manifest merger、D8、APK 打包或签名**。它不是 `assembleDebug` 通过的替代证明。

## 未执行与原因

- 完整 Gradle 构建：当前环境没有完整已配置 Android SDK / Build Tools，Gradle Wrapper 的 Java 下载链路也遭遇 `Network is unreachable`。通过可用下载链路取得编译器、API 和依赖后完成了上述独立检查，没有冒称 APK 构建成功。
- `ChatStoreTest` 的 8 项 Android SQLite 仪器测试：没有连接的 Android 设备或模拟器，已保留在 `app/src/androidTest/`。
- Keystore 迁移、登录与文件选择器、WorkManager 系统调度、真实 WebView 来源边界、渲染退出恢复、真实站点协议覆盖及性能：均需设备执行。

## 在开发机运行

```bash
node --test tests/interceptor.test.cjs
java tests/RunJavaTests.java
./gradlew testDebugUnitTest assembleDebug
./gradlew connectedDebugAndroidTest
```

Windows 将 `./gradlew` 替换为 `.\gradlew.bat`。

## 设备回归清单

1. 使用同一签名覆盖升级，确认旧 Markdown、设置和登录仍存在；密钥迁移失败时应提示重新输入，不崩溃、不清空旧笔记。
2. 开始两个会话并交替聊天，确认原文按会话隔离；重新生成/编辑问题后，总结选定分支，不把两个分支串起来。
3. 正常回复、停止生成、断网、HTTP 失败、重新载入历史分别核对已保存原文；部分内容应标为 partial，不能显示为完整。
4. 第一次整理成功后继续提问，再次整理应生成新笔记；连续点击同一快照应去重；同标题不同任务应同时保留。
5. 整理中离开页面、结束应用进程再启动，核对原文和检查点；任务可能等待系统调度，不能要求即时完成。Android 用户“强行停止”会限制任务运行，需重新打开应用。
6. 模型返回坏 JSON、输出截断、401/429、超时、写盘失败时应显示错误；保存失败后的草稿应可重试，不为相同草稿重新调用模型。
7. 生成中导航、刷新网页、切换会话，已退出网页不得永久遗留“仍在生成”。渲染进程退出后应提供恢复入口，不自动重发用户问题。
8. 外部网页和 iframe 不能向原生提交采集消息；测试文件选择与登录认证路径。桥接能力不支持时明确提示，不能假装全部采集成功。
9. 打开长会话，持续输入、滚动、显示代码/表格/公式、切 Tab、横竖屏、键盘开合、调整字体；至少 30 分钟记录慢帧、内存和错误。
10. 同设备比较系统浏览器、关闭采集的最小 WebView、只启用采集、完整应用。记录实际 WebView 包版本、系统版本、网络、字号和复现步骤，定位剩余排版问题。

本轮修复解决已确认的代码缺陷，不能据此保证第三方网页未来的所有协议或布局始终兼容。
