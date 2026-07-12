// ============================================================
// 企业端 Mock 数据
// ============================================================

// --- ENT-DASH-02 企业工作台 ---
export const entStats = [
  { label: '发布岗位', value: '8 个', color: '#2563EB' },
  { label: '收到申请', value: '45 份', color: '#16A34A' },
  { label: '合作中', value: '3 项', color: '#2563EB' },
  { label: '待处理', value: '12 项', color: '#D97706' },
]

export const entRecentApps = [
  { id: 1, name: '李明  软件工程', job: 'Java开发工程师', date: '2026-03-20', st: '待处理' },
  { id: 2, name: '王芳  计算机科学', job: '前端开发实习生', date: '2026-03-19', st: '待处理' },
  { id: 3, name: '赵强  软件工程', job: '测试工程师', date: '2026-03-18', st: '已处理' },
  { id: 4, name: '陈雪  数据科学', job: '数据分析实习生', date: '2026-03-17', st: '已处理' },
]

export const entActiveJobs = [
  { id: 1, name: 'Java开发工程师', n: 3, st: '招聘中' },
  { id: 2, name: '前端开发实习生', n: 2, st: '招聘中' },
  { id: 3, name: '测试工程师', n: 1, st: '已招满' },
  { id: 4, name: '数据分析实习生', n: 2, st: '招聘中' },
]

export const entProjects = [
  { id: 1, name: '2026春季毕业实习', d: '计算机学院 · 进行中', c: '#2563EB' },
  { id: 2, name: '企业需求对接项目', d: '软件学院 · 进行中', c: '#16A34A' },
  { id: 3, name: '认知实习合作', d: '电子学院 · 已完成', c: '#6B7280' },
]

// --- ENT-JOB-01 实习需求管理 ---
export const entJobList = [
  { id: 1, name: 'Java开发工程师', type: '毕业实习', count: 3, apps: 15, stat: '招聘中' },
  { id: 2, name: '前端开发实习生', type: '毕业实习', count: 2, apps: 8, stat: '招聘中' },
  { id: 3, name: '测试工程师', type: '灵活实习', count: 1, apps: 20, stat: '已招满' },
  { id: 4, name: '数据分析实习生', type: '企业需求对接', count: 2, apps: 6, stat: '招聘中' },
  { id: 5, name: '产品助理', type: '认知实习', count: 1, apps: 3, stat: '已下架' },
]

// --- ENT-APP-01 学生申请管理 ---
export const allJobTitles = [
  'Java开发工程师', '前端开发实习生', '后端开发实习生',
  '测试工程师', '数据分析实习生', '算法实习生',
]

export const entApplicationList = [
  {
    id: 1, name: '张三', school: '无锡学院', major: '物联网工程', grade: '大三', gy: '2023',
    job: '前端开发实习生', time: '2026-06-10', ftime: '2026-06-10 15:30', st: '待处理',
    sid: '20231001001', dept: '物联网工程学院',
    intro: '我是物联网工程专业大三学生，对前端开发有浓厚兴趣。在校期间参与过校级项目开发，熟悉 React 和 Vue 框架。希望能在贵公司获得实习机会...',
  },
  {
    id: 2, name: '李四', school: '无锡学院', major: '计算机科学', grade: '大四', gy: '2022',
    job: '后端开发实习生', time: '2026-06-12', ftime: '2026-06-12 10:00', st: '已通过',
    sid: '20221001002', dept: '计算机学院',
    intro: '计算机科学专业大四学生...',
  },
  {
    id: 3, name: '王五', school: '无锡学院', major: '软件工程', grade: '大三', gy: '2023',
    job: '数据分析实习生', time: '2026-06-14', ftime: '2026-06-14 09:20', st: '已拒绝',
    sid: '20231001003', dept: '软件工程学院',
    intro: '软件工程专业...',
  },
  {
    id: 4, name: '赵六', school: '无锡学院', major: '人工智能', grade: '大四', gy: '2022',
    job: '算法实习生', time: '2026-06-15', ftime: '2026-06-15 14:00', st: '待处理',
    sid: '20221001004', dept: '人工智能学院',
    intro: '人工智能专业大四学生...',
  },
  {
    id: 5, name: '孙七', school: '无锡学院', major: '网络工程', grade: '大三', gy: '2023',
    job: '前端开发实习生', time: '2026-06-16', ftime: '2026-06-16 11:10', st: '待处理',
    sid: '20231001005', dept: '网络工程学院',
    intro: '网络工程专业...',
  },
]

export const appStatusMap = {
  '待处理': { bg: '#FEF3C7', color: '#CA8A04' },
  '已通过': { bg: '#D1FAE5', color: '#16A34A' },
  '已拒绝': { bg: '#FEE2E2', color: '#FF383C' },
}

// --- ENT-INTRN-01 实习对接管理 ---
export const entInternshipList = [
  { id: 1, name: '张三', job: 'Java开发工程师', school: '无锡学院', start: '2026-06-01', prog: '8/12周', stat: '进行中' },
  { id: 2, name: '李四', job: '前端开发实习生', school: '无锡学院', start: '2026-06-15', prog: '6/12周', stat: '进行中' },
  { id: 3, name: '王五', job: '测试工程师', school: '无锡学院', start: '2026-05-20', prog: '12/12周', stat: '已完成' },
  { id: 4, name: '赵六', job: '数据分析实习生', school: '无锡学院', start: '2026-07-01', prog: '4/12周', stat: '进行中' },
]

export const entInternshipStats = [
  { label: '对接学生总数', value: 24, color: '#2563EB' },
  { label: '已完成实习', value: 6, color: '#16A34A' },
  { label: '待评价', value: 5, color: '#D97706' },
  { label: '本周周报提交', value: '15/18', color: '#8B5CF6' },
]

// --- ENT-EVAL-01 企业导师评价 ---
export const evalStudentInfo = {
  name: '王思远', sid: '2024010101', school: '无锡学院', major: '软件工程',
  job: '前端开发工程师', type: '毕业实习',
  period: '2026-03-01 至 2026-08-31', mentor: '陈志远',
}

export const evalDefaultScores = { attitude: 90, skill: 88, teamwork: 85, learning: 92, expression: 86 }

// --- ENT-PROF-01 企业信息管理 ---
export const entProfileContacts = [
  { id: 1, name: '陈志远', title: '技术总监', phone: '0510-88123456', email: 'chenzy@fanruan.com', status: '主联系人' },
  { id: 2, name: '李明明', title: 'HR经理', phone: '0510-88123457', email: 'limm@fanruan.com', status: '普通联系人' },
  { id: 3, name: '王建国', title: '研发主管', phone: '0510-88123458', email: 'wangjg@fanruan.com', status: '企业导师' },
]

// --- ENT-NOTI-01 企业消息中心 ---
export const entMessages = [
  { id: 1, title: '企业资质审核通过', type: 'audit', from: '教务处', time: '2026-06-19 10:30', read: false, content: '恭喜！您的企业资质已通过学校复核，企业后台权限已自动开通。现在您可以：\n1. 在企业工作台查看管理后台\n2. 在实习需求管理中发布实习岗位\n3. 在企业信息管理中完善企业资料\n\n如有疑问请联系学校教务处。' },
  { id: 2, title: '新学生申请通知', type: 'audit', from: '系统通知', time: '2026-06-18 14:20', read: false, content: '张同学申请了您发布的前端开发实习生岗位，请及时在「学生申请管理」页面处理。\n\n申请信息：张同学 | 无锡学院 | 物联网工程 | 大三\n申请时间：2026-06-18 14:20\n\n请在3个工作日内完成处理。' },
  { id: 3, title: '实习周报提交通知', type: 'report', from: '系统通知', time: '2026-06-17 09:15', read: true, content: '王同学提交了第4周实习周报，请及时查看并批阅。\n\n学生：王思远 | 前端开发工程师 | 毕业实习\n周报周期：2026/06/10 – 2026/06/16\n提交时间：2026-06-17 09:15\n\n请前往「实习对接管理」页面进行批阅。' },
  { id: 4, title: '实习计划发布通知', type: 'notice', from: '学校管理员', time: '2026-06-15 16:00', read: true, content: '学校已发布2026届毕业实习计划，请关注相关时间节点要求：\n\n1. 实习周期：2026年7月至2027年6月\n2. 学生申请截止：2026年7月15日\n3. 企业审核截止：2026年7月20日\n4. 实习开始：2026年7月下旬\n\n请合理安排岗位发布和对接计划。' },
]

// --- ENT 侧边栏菜单 ---
export const entMenuItems = [
  { to: '/company/dashboard', label: '企业工作台' },
  { to: '/company/profile', label: '企业信息管理' },
  { to: '/company/jobs', label: '实习需求管理' },
  { to: '/company/applications', label: '学生申请管理' },
  { to: '/company/internships', label: '实习对接管理' },
  { to: '/company/notifications', label: '消息中心' },
]
