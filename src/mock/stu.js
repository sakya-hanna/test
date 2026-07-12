export const dashboardStats = {
  // 统计卡片
  statCards: [
    { label: '全部实习', value: 2, color: '#2563EB' },
    { label: '进行中', value: 1, color: '#16A34A' },
    { label: '待办事项', value: 3, color: '#F59E0B' },
    { label: '已完成', value: 1, color: '#6B7280' },
  ],
  // 当前实习
  currentInternship: { type: '毕业实习', company: '无锡XX科技有限公司', position: '前端开发实习生', status: '进行中', startDate: '2026-03-20', endDate: '2026-06-20' },
  // 进度阶段
  progressStages: [
    { label: '提交申请', done: true },
    { label: '指导老师审核', done: true },
    { label: '学院复核', done: true },
    { label: '进行中', done: true, current: true },
    { label: '实习结束', done: false },
  ],
  // 待办事项
  pendingItems: [
    { id: 1, text: '提交第3月实习月报', deadline: '2026-06-25', status: '待提交', link: '/stu/reports/monthly/new' },
    { id: 2, text: '提交实习成果材料', deadline: '2026-06-30', status: '待提交', link: '/stu/achievements/new' },
    { id: 3, text: '填写实习中期总结', deadline: '2026-06-20', status: '待提交', link: '/stu/internships/1/mid-summary' },
    { id: 4, text: '完成企业导师评价', deadline: '2026-06-28', status: '待提交', link: '/stu/internships/1/mentor-eval' },
  ],
  // 通知
  recentNotices: [
    { id: 1, title: '关于2026届毕业实习材料提交的通知', time: '2026-06-15', from: '教务处' },
    { id: 2, title: '企业导师评价已开放', time: '2026-06-10', from: '实习管理科' },
    { id: 3, title: '暑期社会实践报名通知', time: '2026-06-05', from: '校团委' },
  ],
  // 消息
  messages: [
    { id: 1, title: '你的毕业实习申请已通过学院复核', from: '系统通知', time: '2026-06-23 16:00', read: false, content: '张同学你好，你的毕业实习申请（华为技术有限公司·软件开发工程师）已通过学院复核，请及时关注后续通知，按要求完成实习周报与月报提交。' },
    { id: 2, title: '第3周实习周报批阅完成', from: '批阅通知', time: '2026-06-22 14:30', read: false, content: '李教授已批阅你的第3周实习周报，评级：良好。评语：内容详实，技术总结到位，建议增加对业务理解的部分。' },
    { id: 3, title: '6月实习月报提交提醒', from: '系统提醒', time: '2026-06-21 09:00', read: true, content: '请于6月25日前提交6月实习月报，逾期将影响实习成绩评定。月报需包含本月工作内容、学习心得、存在问题及下月计划。' },
    { id: 4, title: '企业导师评价已开放', from: '系统通知', time: '2026-06-20 10:00', read: true, content: '企业导师评价通道已开放，请联系你的企业指导老师完成对你的实习评价。评价截止日期：2026年6月28日。' },
    { id: 5, title: '毕业实习材料提交通知', from: '教务处', time: '2026-06-15 08:00', read: true, content: '根据学校安排，2026届毕业实习材料提交截止时间为6月30日，请按时提交实习成果材料、实习报告等。逾期未提交将无法按时完成实习成绩评定。' },
  ],
  // 快捷入口
  quickActions: [
    { label: '我的实习', icon: '📋', link: '/stu/internships/overview' },
    { label: '提交周报', icon: '📝', link: '/stu/reports/weekly/new' },
    { label: '企业需求', icon: '🏢', link: '/stu/jobs' },
    { label: '成绩查看', icon: '📊', link: '/stu/evaluations' },
  ],
}

export const internshipList = [
  { id: 1, type: '毕业实习', company: '华为技术有限公司', position: '软件开发工程师', applyDate: '2026-05-20', status: '审核中' },
  { id: 2, type: '毕业实习', company: '腾讯科技有限公司', position: '产品经理助理', applyDate: '2026-05-18', status: '进行中' },
  { id: 3, type: '灵活实习', company: '字节跳动科技有限公司', position: '数据分析实习生', applyDate: '2026-05-15', status: '已完成' },
  { id: 4, type: '认知实习', company: '阿里巴巴集团', position: '技术参观', applyDate: '2026-05-10', status: '已驳回' },
  { id: 5, type: '毕业实习', company: '美团科技有限公司', position: '后端开发实习生', applyDate: '2026-05-08', status: '已完成' },
]

// 实习列表页统计卡片
export const internshipStats = [
  { label: '全部实习', value: 5, unit: '个申请', color: '#2563EB' },
  { label: '审核中', value: 2, unit: '等待审核', color: '#F59E0B' },
  { label: '进行中', value: 1, unit: '实习进行中', color: '#16A34A' },
  { label: '已完成', value: 2, unit: '已结束', color: '#6B7280' },
]

// 实习类型筛选标签
export const internshipTypeTabs = [
  { label: '全部', value: '' },
  { label: '毕业实习', value: '毕业实习' },
  { label: '灵活实习', value: '灵活实习' },
  { label: '认知实习', value: '认知实习' },
  { label: '社会实践', value: '社会实践' },
]

// 状态映射：颜色与文案
export const internshipStatusMap = {
  '审核中': { color: '#F59E0B', bg: 'rgba(245,158,11,0.1)' },
  '进行中': { color: '#2563EB', bg: 'rgba(37,99,235,0.1)' },
  '已完成': { color: '#16A34A', bg: 'rgba(20,141,61,0.25)' },
  '已驳回': { color: '#FF383C', bg: 'rgba(255,56,60,0.25)' },
}

// ========== STU-JOB-01 企业需求浏览 ==========

export const jobStats = [
  { label: '全部岗位', value: 24, color: '#2563EB' },
  { label: '技术类', value: 12, color: '#7C3AED' },
  { label: '管理类', value: 6, color: '#F59E0B' },
  { label: '已报名', value: 2, color: '#16A34A' },
]

export const jobTypeTabs = [
  { label: '全部', value: '' },
  { label: '毕业实习', value: '毕业实习' },
  { label: '灵活实习', value: '灵活实习' },
  { label: '认知实习', value: '认知实习' },
  { label: '社会实践', value: '社会实践' },
]

export const jobListings = [
  {
    id: 1,
    company: '华为技术有限公司',
    logo: '华',
    position: '软件开发工程师（实习）',
    type: '毕业实习',
    location: '深圳',
    education: '本科',
    vacancies: '招5人',
    publishDate: '2026-06-20',
    deadline: '2026-07-15',
    description: '参与5G通信系统后端开发，负责模块设计与编码实现...',
    tags: ['Java', 'Spring', 'MySQL'],
  },
  {
    id: 2,
    company: '腾讯科技有限公司',
    logo: '腾',
    position: '产品经理助理（实习）',
    type: '毕业实习',
    location: '北京',
    education: '本科及以上',
    vacancies: '招3人',
    publishDate: '2026-06-18',
    deadline: '2026-07-10',
    description: '协助产品经理进行需求分析、竞品调研与产品迭代...',
    tags: ['产品设计', '数据分析', 'Axure'],
  },
  {
    id: 3,
    company: '字节跳动科技有限公司',
    logo: '字',
    position: '数据分析实习生',
    type: '灵活实习',
    location: '上海',
    education: '本科',
    vacancies: '招2人',
    publishDate: '2026-06-15',
    deadline: '2026-07-20',
    description: '负责业务数据采集、清洗与分析，输出数据报告...',
    tags: ['Python', 'SQL', '数据可视化'],
  },
  {
    id: 4,
    company: '阿里巴巴集团',
    logo: '阿',
    position: '前端开发实习生',
    type: '毕业实习',
    location: '杭州',
    education: '本科及以上',
    vacancies: '招4人',
    publishDate: '2026-06-12',
    deadline: '2026-07-12',
    description: '参与电商平台前端开发，优化用户体验和页面性能...',
    tags: ['React', 'TypeScript', 'Node.js'],
  },
  {
    id: 5,
    company: '美团科技有限公司',
    logo: '美',
    position: '后端开发实习生',
    type: '毕业实习',
    location: '北京',
    education: '本科',
    vacancies: '招3人',
    publishDate: '2026-06-10',
    deadline: '2026-07-18',
    description: '参与外卖配送系统后端服务开发，优化调度算法...',
    tags: ['Go', '微服务', 'K8s'],
  },
  {
    id: 6,
    company: '比亚迪股份有限公司',
    logo: '比',
    position: '嵌入式开发实习生',
    type: '毕业实习',
    location: '深圳',
    education: '本科',
    vacancies: '招6人',
    publishDate: '2026-06-08',
    deadline: '2026-08-01',
    description: '参与新能源汽车电控系统嵌入式软件开发...',
    tags: ['C/C++', 'RTOS', 'ARM'],
  },
]

// ========== STU-EVAL-01 成绩查看 ==========

export const evaluationData = {
  // 综合成绩
  composite: {
    total: 87.5,
    schoolScore: 85,
    schoolWeight: 50,
    enterpriseScore: 90,
    enterpriseWeight: 50,
    grade: '良好',
    gradeColor: '#16A34A',
  },
  // 分项评分
  items: [
    { name: '实习态度与纪律', weight: '20%', schoolScore: 88, enterpriseScore: 92, composite: 90 },
    { name: '专业技能与学习能力', weight: '25%', schoolScore: 82, enterpriseScore: 90, composite: 86 },
    { name: '工作成果与质量', weight: '25%', schoolScore: 85, enterpriseScore: 88, composite: 86.5 },
    { name: '沟通协作与团队精神', weight: '15%', schoolScore: 90, enterpriseScore: 92, composite: 91 },
    { name: '创新与解决问题能力', weight: '15%', schoolScore: 80, enterpriseScore: 88, composite: 84 },
  ],
  // 导师评语
  comments: {
    schoolMentor: {
      name: '李教授',
      role: '学校指导老师',
      content: '该生在实习期间表现优秀，态度端正，能够按时完成各项任务。在技术学习方面积极主动，遇到问题能够独立思考并寻求解决方案。建议在今后的工作中进一步加强对业务的理解深度，提升系统设计能力。总体表现良好，达到了毕业实习的预期目标。',
      date: '2026-06-22',
    },
    enterpriseMentor: {
      name: '王经理',
      role: '企业指导老师',
      content: '张同学在实习期间展现了扎实的专业基础和良好的学习能力，能够快速融入团队并承担实际开发任务。在项目协作中沟通顺畅，代码质量较高，按时交付了多个功能模块。希望继续保持学习热情，在系统架构方面深入探索。综合评定：优秀。',
      date: '2026-06-21',
    },
  },
  // 实习基本信息
  info: {
    type: '毕业实习',
    company: '华为技术有限公司',
    position: '软件开发工程师（前端方向）',
    period: '2026-03-20 ~ 2026-06-20',
    totalDays: 92,
  },
}

// ========== STU-PAR-01 家长知情 ==========

export const parentNoticeData = {
  status: {
    label: '审核中',
    description: '回执已提交，等待学院审核',
    deadline: '2026-07-05',
  },
  student: {
    name: '张同学',
    studentNo: '202301080126',
    majorClass: '软件工程 2301 班',
  },
  internship: {
    company: '华为技术有限公司',
    position: '软件开发工程师（前端方向）',
    period: '2026-07-15 至 2026-10-15',
    address: '江苏省无锡市滨湖区软件园',
  },
  parent: {
    name: '张建国',
    relation: '父亲',
    phone: '138****5621',
  },
  notice: {
    title: '学生校外实习家长知情告知书',
    version: '2026 年毕业实习版',
    updatedAt: '2026-06-28',
    summary: '请家长充分了解实习单位、岗位、周期及安全责任，阅读告知事项后签字确认，并由学生上传清晰回执。',
  },
  receipt: {
    fileName: '家长知情回执_张同学.pdf',
    uploadedAt: '2026-06-30 16:24',
    fileSize: '1.8 MB',
    reviewer: '学院实习管理老师',
  },
}

// ========== STU-RPT-00 过程记录入口 ==========

export const processRecordStats = [
  { label: '周报已提交', value: 8, unit: '篇', color: '#2563EB' },
  { label: '月报已提交', value: 2, unit: '篇', color: '#16A34A' },
  { label: '成果已提交', value: 1, unit: '项', color: '#7C3AED' },
  { label: '待提交', value: 3, unit: '项', color: '#F59E0B' },
]

export const recentSubmissions = [
  { id: 1, type: '周报', title: '第8周实习周报', summary: '本周完成了订单模块的接口联调，修复了3个线上bug，参与了代码评审...', date: '2026-06-28', status: '已提交', statusColor: '#16A34A', link: '/stu/reports/weekly/new' },
  { id: 2, type: '月报', title: '6月实习月报', summary: '本月完成了用户中心模块的开发，学习了K8s基础部署，总结了实习心得...', date: '2026-06-25', status: '已提交', statusColor: '#16A34A', link: '/stu/reports/monthly/new' },
  { id: 3, type: '周报', title: '第7周实习周报', summary: '本周主要参与了前端重构项目，使用Vue3+TS重写了两个核心页面...', date: '2026-06-21', status: '已批阅', statusColor: '#2563EB', link: '/stu/reports/weekly/new' },
  { id: 4, type: '月报', title: '5月实习月报', summary: '本月熟悉了团队开发流程，完成了第一个独立功能模块的开发和测试...', date: '2026-05-28', status: '已批阅', statusColor: '#2563EB', link: '/stu/reports/monthly/new' },
  { id: 5, type: '成果', title: '毕业实习成果提交', summary: '包含实习报告、鉴定表、单位回执等材料...', date: '2026-06-20', status: '审核中', statusColor: '#F59E0B', link: '/stu/achievements/new' },
]

export const quickEntryCards = [
  {
    key: 'weekly',
    title: '提交周报',
    desc: '每周实习总结与心得',
    icon: '📝',
    link: '/stu/reports/weekly/new',
    hint: '本周未提交',
    hintColor: '#FF383C',
  },
  {
    key: 'monthly',
    title: '提交月报',
    desc: '每月实习总结与计划',
    icon: '📊',
    link: '/stu/reports/monthly/new',
    hint: '本月已提交',
    hintColor: '#16A34A',
  },
  {
    key: 'achievement',
    title: '提交成果',
    desc: '实习报告与结项材料',
    icon: '📄',
    link: '/stu/achievements/new',
    hint: '待提交材料',
    hintColor: '#F59E0B',
  },
]

export const reportSchedule = {
  weekly: { submitted: 8, total: 12, nextDeadline: '2026-07-05' },
  monthly: { submitted: 2, total: 3, nextDeadline: '2026-07-25' },
  achievement: { submitted: 1, total: 4, deadline: '2026-06-30' },
}

// ========== STU-RPT-03-MAT 材料提交入口 ==========

export const materialStats = [
  { label: '全部材料', value: 6, unit: '项', color: '#2563EB' },
  { label: '已提交', value: 3, unit: '项', color: '#16A34A' },
  { label: '待上传', value: 2, unit: '项', color: '#F59E0B' },
  { label: '审核中', value: 1, unit: '项', color: '#7C3AED' },
]

export const materialChecklist = [
  {
    id: 1,
    name: '实习报告',
    desc: '毕业实习总结报告，不少于3000字，包含实习内容、收获体会、问题与建议',
    required: true,
    status: '已上传',
    file: '毕业实习报告_张同学.docx',
    fileSize: '2.4 MB',
    deadline: '2026-06-30',
    uploadedAt: '2026-06-28 14:30',
  },
  {
    id: 2,
    name: '实习鉴定表',
    desc: '由企业指导老师填写并加盖公章的实习鉴定表',
    required: true,
    status: '待上传',
    file: '',
    fileSize: '',
    deadline: '2026-06-30',
    uploadedAt: '',
  },
  {
    id: 3,
    name: '实习单位回执',
    desc: '实习单位出具的实习证明回执，需加盖单位公章',
    required: true,
    status: '审核中',
    file: '实习回执_扫描件.pdf',
    fileSize: '1.2 MB',
    deadline: '2026-06-30',
    uploadedAt: '2026-06-25 10:15',
  },
  {
    id: 4,
    name: '个人简历',
    desc: '最新版个人简历（PDF格式）',
    required: true,
    status: '已上传',
    file: '个人简历_张同学.pdf',
    fileSize: '856 KB',
    deadline: '2026-06-30',
    uploadedAt: '2026-06-20 09:00',
  },
  {
    id: 5,
    name: '获奖证书/项目成果',
    desc: '在校期间获得的奖项证书、项目成果证明等（选填）',
    required: false,
    status: '已上传',
    file: '获奖证书_合集.pdf',
    fileSize: '3.1 MB',
    deadline: '2026-06-30',
    uploadedAt: '2026-06-22 16:45',
  },
  {
    id: 6,
    name: '其他补充材料',
    desc: '如英语等级证书、职业资格证书等（选填）',
    required: false,
    status: '待上传',
    file: '',
    fileSize: '',
    deadline: '2026-06-30',
    uploadedAt: '',
  },
]

export const materialSubmissionProgress = {
  submitted: 3,
  total: 6,
  requiredTotal: 4,
  requiredSubmitted: 2,
  deadline: '2026-06-30',
  remainingDays: 28,
}
