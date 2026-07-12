# Prototype Quality Sprint Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在三天内按问题类别收敛全仓库 Vue 展示原型，重点提升视觉完成度、页面跳转关系和运行稳定性，而非实现真实业务功能。

**Architecture:** 保留现有 Vue 3、Vue Router 和 Tailwind CSS 结构，复用既有 Layout 与页面视觉模式。导航控件负责正确跳转，非导航展示控件只提供轻量弹层、标签切换或说明提示；不建设复杂 mock 状态机、跨页面数据同步或持久化。

**Tech Stack:** Vue 3 `<script setup>`、Vue Router 4、Tailwind CSS 4、Vite 8、Node.js test runner。

---

## Chunk 0: 《问题汇总》优先修复

### Task 1: 修复 PC 登录页视觉居中

**Files:**
- Modify: `src/views/auth/Login.vue`
- Test: `tests/prototype-quality-contract.test.mjs`

- [ ] 编写失败契约，确认登录页仍包含固定横向坐标并因此无法随视口居中。
- [ ] 将 Logo、标题和表单组成有限宽度内容组，由外层容器水平居中。
- [ ] 保留背景、字号、颜色、表单尺寸和接近 Figma 的垂直视觉重心。
- [ ] 检查常用 PC 视口不横向溢出；不增加真实认证和复杂校验。
- [ ] 运行契约测试、`npm test` 和 `npm run build`。
- [ ] 提交：`fix: center the login presentation`。

### Task 2: 调整企业注册展示路径

**Files:**
- Modify: `src/views/auth/Login.vue`
- Modify: `src/views/ent/PortalLanding.vue`
- Modify: `src/views/auth/RegisterEntry.vue`
- Reference: `src/router/auth.js`
- Reference: `src/router/ent.js`
- Test: `tests/prototype-quality-contract.test.mjs`

- [ ] 编写失败契约，表达目标路径 `/login → /portal → /company/register-entry → /company/register`。
- [ ] 将登录页“企业注册”入口改为 `/portal`。
- [ ] 将 `/portal` 的主视觉和文案明确为学校/校企合作背景介绍，并让注册 CTA 进入 `/company/register-entry`。
- [ ] 保留账号注册页到四步注册向导 `/company/register` 的跳转。
- [ ] 验证每一步都有可见入口和返回路径，不新增重复背景介绍路由。
- [ ] 运行测试和构建。
- [ ] 提交：`fix: route enterprise registration through portal intro`。

### Task 3: 补齐企业指导老师展示入口

**Files:**
- Create: `src/views/ent/MentorList.vue`
- Modify: `src/router/ent.js`
- Modify: `src/views/ent/Dashboard.vue`
- Modify: `src/views/ent/ApplicationList.vue`
- Modify: `src/views/ent/JobList.vue`
- Modify: `src/views/ent/JobEdit.vue`
- Modify: `src/views/ent/InternshipList.vue`
- Modify: `src/views/ent/Profile.vue`
- Modify as needed: remaining `src/views/ent/*.vue`
- Test: `tests/prototype-quality-contract.test.mjs`

- [ ] 编写失败契约，扫描企业侧边栏中的“企业指导老师”空链接。
- [ ] 新增 `/company/mentors` 路由和纯展示页面；不虚构永久页面编号或导师管理业务流程。
- [ ] 页面使用企业端现有视觉语言，展示标题、摘要卡片、静态列表和空态/说明。
- [ ] 将所有企业侧边栏的该入口统一改为 `/company/mentors`。
- [ ] 验证入口、激活态和返回企业工作台路径。
- [ ] 运行测试和构建。
- [ ] 提交：`feat: add enterprise mentor presentation page`。

### Task 4: 修正企业岗位发布展示字段

**Files:**
- Modify: `src/views/ent/JobList.vue`
- Modify: `src/views/ent/JobEdit.vue`
- Test: `tests/prototype-quality-contract.test.mjs`

- [ ] 编写失败契约，确认两个企业发布表单仍显示“实习类型”。
- [ ] 从内联发布表单和独立发布/编辑页移除“实习类型”选择及对应表单字段。
- [ ] 重新平衡剩余字段的栅格、对齐和间距，不改变已发布列表的学校分类展示规则。
- [ ] 回归最新 `main` 已修复的右侧已发布栏位对齐，避免覆盖同学改动。
- [ ] 运行测试和构建。
- [ ] 提交：`fix: remove internship type from enterprise job forms`。

## Chunk 1: 视觉与稳定性基线

### Task 5: 建立展示原型质量契约

**Files:**
- Create: `tests/prototype-quality-contract.test.mjs`
- Reference: `agent.md`
- Reference: `docs/页面分类优化进度.md`

- [ ] 编写失败测试，检查 PC 登录页不再使用 `left-[641px]`、`left-[613px]`、`left-[494px]` 等固定横向坐标。
- [ ] 编写失败测试，扫描 `href="#"`、空路由目标和“待 Figma 设计还原”等显眼占位文案。
- [ ] 将《问题汇总》的登录居中、注册路径、导师入口和岗位字段加入回归契约。
- [ ] 编写测试，确认核心路由文件引用的 Vue 页面均存在。
- [ ] 运行 `node --test tests/prototype-quality-contract.test.mjs`，确认测试因现有问题失败。
- [ ] 提交：`test: add prototype presentation quality contracts`。

### Task 6: 统一其余认证页面展示路径

**Files:**
- Modify: `src/views/auth/MobileLogin.vue`
- Modify: `src/views/auth/PadLogin.vue`
- Modify: `src/views/auth/SSOAccount.vue`
- Modify: `src/views/auth/SSOSms.vue`
- Modify as needed: `src/views/auth/SSOQr.vue`
- Test: `tests/prototype-quality-contract.test.mjs`

- [ ] 检查 PC、移动、平板、账号、短信和扫码页面的视觉居中与内容溢出。
- [ ] 登录按钮只需跳到对应演示首页或显示轻量原型提示。
- [ ] 验证认证方式切换及返回 PC 登录的链接均可到达。
- [ ] 运行测试和构建。
- [ ] 提交：`fix: align authentication presentation routes`。

## Chunk 2: 空页面与视觉完成度

### Task 7: 补全学院端占位页面

**Files:**
- Modify: `src/views/col/Dashboard.vue`
- Modify: `src/views/col/CompanyReview.vue`
- Modify: `src/views/col/NotificationManage.vue`
- Reuse: `src/layouts/LayoutCollege.vue`
- Reference: `src/views/col/ApplicationReview.vue`
- Reference: `src/views/sch/CompanyReview.vue`
- Reference: `src/views/sch/NotificationManage.vue`
- Test: `tests/prototype-quality-contract.test.mjs`

- [ ] 为学院工作台补统计卡片、待办摘要和快捷入口，保持已有设计令牌。
- [ ] 参考学校复核页补企业初审的筛选区、表格和审核弹层视觉。
- [ ] 参考学校通知页补学院通知列表、发布区和空态视觉。
- [ ] 仅保证弹层可打开、标签可切换、入口可跳转，不实现审核或发布状态机。
- [ ] 清除“待 Figma 设计还原”文案并运行测试、构建。
- [ ] 提交：`feat: complete college presentation pages`。

### Task 8: 补全系统管理端空内容区

**Files:**
- Modify: `src/views/sys/UserManage.vue`
- Modify: `src/views/sys/RoleConfig.vue`
- Modify: `src/views/sys/GlobalConfig.vue`
- Modify: `src/views/sys/SystemLogs.vue`
- Modify: `src/views/sys/ErrorHandle.vue`
- Reuse: `src/views/sys/Dashboard.vue`
- Test: `tests/prototype-quality-contract.test.mjs`

- [ ] 用户管理补静态筛选区、用户表格和状态标签。
- [ ] 权限配置补角色列表和权限矩阵展示。
- [ ] 全局配置补分组配置卡片；系统日志和异常处理补筛选、表格与空态。
- [ ] 操作按钮只提供轻量提示或弹层，不实现真实权限、配置保存和日志处理。
- [ ] 检查页面间距、表格宽度、状态颜色和常用 PC 视口溢出。
- [ ] 运行测试和构建。
- [ ] 提交：`feat: complete system presentation pages`。

### Task 9: 全仓库视觉一致性扫描

**Files:**
- Modify as needed: `src/views/**/*.vue`
- Modify as needed: `src/layouts/*.vue`
- Reference: `agent.md`

- [ ] 检查标题、副标题、卡片圆角、边框、主次按钮和状态色是否符合 `agent.md` 设计令牌。
- [ ] 检查明显纯白空内容区、内容重叠、横向溢出和高度断层。
- [ ] 检查空列表是否具有说明文本与下一步入口。
- [ ] 只修复明显不一致，不重画已经可验收的页面。
- [ ] 运行测试和构建。
- [ ] 提交：`style: align prototype presentation surfaces`。

## Chunk 3: 跳转关系与轻量响应

### Task 10: 清理静默导航与断头页面

**Files:**
- Modify as needed: `src/router/*.js`
- Modify as needed: `src/layouts/*.vue`
- Modify as needed: relevant `src/views/**/*.vue`
- Reference: `实习管理系统页面跳转关系与路由规划.md`
- Test: `tests/prototype-quality-contract.test.mjs`

- [ ] 逐项检查侧边栏、顶部入口、快捷卡片和列表“查看/详情”按钮。
- [ ] 将导航控件指向已注册路由，移除 `href="#"` 和空目标。
- [ ] 为每个详情、表单和子页面补一个可见入口与明确返回按钮。
- [ ] 对没有确定目标的控件显示“原型展示，目标待确认”，不擅自新增业务路由。
- [ ] 运行测试和构建。
- [ ] 提交：`fix: close prototype navigation paths`。

### Task 11: 处理非导航展示按钮

**Files:**
- Modify as needed: `src/views/ent/*.vue`
- Modify as needed: `src/views/sch/*.vue`
- Modify as needed: `src/views/cou/*.vue`
- Modify as needed: `src/views/stu/*.vue`
- Modify as needed: `src/views/tut/*.vue`

- [ ] 将弹层、标签、预览等展示控件接到现有局部 UI 状态。
- [ ] 上传、导出、提交、删除等功能型按钮仅提供轻量原型提示或说明。
- [ ] 不实现表单校验、数据持久化、审核状态机或跨页面 mock 同步。
- [ ] 纯装饰元素移除按钮语义，避免制造可点击预期。
- [ ] 运行测试和构建。
- [ ] 提交：`fix: make presentation controls explicit`。

## Chunk 4: 展示路径与无错验收

### Task 12: 验证三条核心展示路径

**Files:**
- Modify as needed: `src/router/*.js`
- Modify as needed: relevant `src/views/**/*.vue`
- Test: `tests/prototype-quality-contract.test.mjs`

- [ ] 点击验证“岗位 → 申请 → 实习 → 过程记录/材料”页面关系。
- [ ] 点击验证“教师初审 → 学院复核 → 学校/企业状态”页面关系。
- [ ] 点击验证“企业评价 → 教师评定 → 学生成绩查看”页面关系。
- [ ] 只要求页面可达、视觉叙事一致和返回路径完整，不要求前后页面数据联动。
- [ ] 检查浏览器控制台无红错并运行完整测试、构建。
- [ ] 提交：`fix: verify critical presentation journeys`。

### Task 13: 更新交付文档和最终验收

**Files:**
- Modify: `README.md`
- Modify: `docs/页面分类优化进度.md`
- Modify as needed: `docs/路由表.md`

- [ ] README 补项目定位、安装运行、角色入口、推荐展示路径和原型边界。
- [ ] 明确声明按钮响应仅服务展示，不代表真实业务功能已实现。
- [ ] 更新分类进度表的实际状态和剩余待确认项。
- [ ] 同步更新 `问题汇总.md` 的解决状态，并保留已发布栏位对齐为回归通过项。
- [ ] 执行 `npm test`，期望全部通过。
- [ ] 执行 `npm run build`，期望 Vite 构建成功。
- [ ] 人工检查核心页面和浏览器控制台，确认无明显错位、断链和红错。
- [ ] 提交：`docs: finalize prototype presentation guide`。
