# 无锡学院实习管理系统 — 交互原型

Vue 3 交互原型项目，用于展示和验证实习管理系统的页面设计、跳转关系和交互流程。

## 技术栈

- Vue 3 `<script setup>` SFC
- Vue Router 4
- Tailwind CSS 4
- Vite 8
- Node.js test runner

## 快速开始

```bash
npm install
npm run dev      # 开发服务器 http://localhost:5173
npm test         # 运行契约测试（14 项）
npm run build    # 生产构建
```

## 角色入口

| 角色 | 入口路由 | 默认首页 |
|------|----------|----------|
| 学生 | `/login` | `/stu/dashboard` |
| 指导老师 | `/login` | `/teacher/dashboard` |
| 学院管理 | `/login` | `/college/dashboard` |
| 学校管理 | `/login` | `/school/dashboard` |
| 系统管理 | `/login` | `/admin/dashboard` |
| 辅导员 | `/login` | `/counselor/dashboard` |
| 企业 | `/portal` → 注册/登录 | `/company/dashboard` |
| 公共认证 | `/login` | SSO / 忘记密码 / 移动端 / 平板端 |

## 推荐展示路径

**学生申请链路：**
`/stu/jobs` → `/stu/internships/new` → `/stu/internships` → `/stu/internships/1` → `/stu/reports`

**审核流转链路：**
`/teacher/applications` → `/college/applications/review`

**评价与成绩链路：**
`/company/internships/1/evaluation` → `/teacher/grades` → `/stu/evaluations`

**企业入驻链路：**
`/portal` → `/company/register`

## 全仓库路由

| 模块 | 路由数 | 示例 |
|------|--------|------|
| 公共认证 | 8 | `/login` `/forgot-password` `/sso/*` `/m/login` `/pad/login` |
| 学生端 | 14 | `/stu/dashboard` `/stu/internships` `/stu/jobs` `/stu/reports` |
| 教师端 | 6 | `/teacher/dashboard` `/teacher/applications` `/teacher/grades` |
| 学院端 | 5 | `/college/dashboard` `/college/companies/review` `/college/applications/review` |
| 学校端 | 6 | `/school/dashboard` `/school/companies/review` `/school/analytics` |
| 企业端 | 12 | `/company/dashboard` `/company/jobs` `/company/mentors` `/portal` |
| 辅导员端 | 4 | `/counselor/dashboard` `/counselor/students` `/counselor/alerts` |
| 系统管理 | 6 | `/admin/dashboard` `/admin/users` `/admin/settings` `/admin/logs` |
| 公共页面 | 3 | `/notifications` `/notifications/:id` `*` (404) |

## 原型边界

> ⚠️ 本项目为展示原型，不承担真实业务功能。

- **按钮响应：** 导航按钮可跳转；操作按钮（导出/上传/提交/删除）仅显示演示提示
- **无后端：** 不实现真实 API、数据库、身份认证、文件上传、权限控制
- **无状态机：** 不实现完整表单校验、审核状态机、跨页面数据同步或持久化
- **静态数据：** 表格和列表使用内联 mock 数据，刷新后重置
- **未确认业务：** 标记「待确认」，不擅自推断流转规则

## 文档索引

- `问题汇总.md` — 原型已知问题清单
- `docs/页面分类优化进度.md` — 按类别跟踪优化状态
- `docs/路由表.md` — 路由路径速查
- `docs/页面地图.md` — 页面关系概览
- `实习管理系统页面跳转关系与路由规划.md` — 跳转关系完整文档
- `实习管理系统前端分阶段开发规范.md` — 开发规范
- `实习管理系统页面开发总控进度表.md` — 页面开发总控
- `agent.md` — 项目上下文（CLAUDE.md 同目录）
