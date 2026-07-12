// ========== TUT-DASH-01 教师工作台 ==========

export const tutDashboardStats = {
  statCards: [
    { label: '指导学生', value: 12, color: '#2563EB' },
    { label: '待审核申请', value: 5, color: '#F59E0B' },
    { label: '待批阅周报', value: 8, color: '#7C3AED' },
    { label: '本月已完成', value: 15, color: '#16A34A' },
  ],
  pendingItems: [
    { id: 1, text: '审核张同学的实习申请表', deadline: '2026-06-24', type: '审核', link: '/teacher/applications' },
    { id: 2, text: '批阅王同学的实习周报（第3周）', deadline: '2026-06-25', type: '批阅', link: '/teacher/reports' },
    { id: 3, text: '批阅李同学的实习月报（6月）', deadline: '2026-06-26', type: '批阅', link: '/teacher/reports' },
    { id: 4, text: '完成赵同学实习成绩评定', deadline: '2026-06-28', type: '评定', link: '/teacher/grades' },
    { id: 5, text: '查看刘同学实习异常预警', deadline: '2026-06-25', type: '跟踪', link: '/teacher/students' },
  ],
  recentNotices: [
    { id: 1, title: '关于2026届毕业实习中期检查的通知', time: '2026-06-20', from: '教务处' },
    { id: 2, title: '指导教师工作规范（2026修订版）已发布', time: '2026-06-18', from: '实习管理科' },
    { id: 3, title: '关于实习成绩评定标准的说明', time: '2026-06-15', from: '教务处' },
  ],
  messages: [
    { id: 1, title: '张同学的实习申请待审核', from: '审核提醒', time: '2026-06-23 15:00', read: false, content: '学生张同学（20231001）提交了毕业实习申请，企业：华为技术有限公司，岗位：软件开发工程师。请尽快完成初审。' },
    { id: 2, title: '王同学的周报待批阅', from: '批阅提醒', time: '2026-06-22 10:00', read: false, content: '学生王同学（20231002）提交了第3周实习周报，请及时批阅并给出评级与意见。' },
    { id: 3, title: '实习管理规范培训通知', from: '教务处', time: '2026-06-20 14:00', read: true, content: '教务处将于6月25日下午2点在行政楼201举办实习指导教师培训，请各位指导老师准时参加。' },
    { id: 4, title: '6月成绩评定截止提醒', from: '系统提醒', time: '2026-06-18 09:00', read: true, content: '本月实习成绩评定截止日期为6月30日，目前你仍有3位学生的成绩待评定，请合理安排时间。' },
    { id: 5, title: '实习中期检查通知', from: '实习管理科', time: '2026-06-15 11:00', read: true, content: '根据学校安排，6月下旬将进行实习中期检查，请各位指导老师关注所带学生的实习进展，及时处理异常情况。' },
  ],
  quickActions: [
    { label: '申请审核', icon: '📋', link: '/teacher/applications', color: '#2563EB' },
    { label: '周报批阅', icon: '📝', link: '/teacher/reports', color: '#7C3AED' },
    { label: '成绩评定', icon: '📊', link: '/teacher/grades', color: '#F59E0B' },
    { label: '学生列表', icon: '👥', link: '/teacher/students', color: '#16A34A' },
  ],
}

// ========== TUT-AUD-01 实习申请审核 ==========

export const applicationReviewList = [
  { id: 1, studentName: '张同学', studentNo: '20231001', type: '毕业实习', company: '华为技术有限公司', position: '软件开发工程师（前端方向）', applyDate: '2026-06-20', status: '待审核', department: '计算机科学与技术学院' },
  { id: 2, studentName: '王同学', studentNo: '20231002', type: '毕业实习', company: '腾讯科技有限公司', position: '产品经理助理', applyDate: '2026-06-18', status: '待审核', department: '计算机科学与技术学院' },
  { id: 3, studentName: '赵同学', studentNo: '20231015', type: '灵活实习', company: '字节跳动科技有限公司', position: '数据分析实习生', applyDate: '2026-06-15', status: '已通过', department: '数学与统计学院' },
  { id: 4, studentName: '刘同学', studentNo: '20231008', type: '毕业实习', company: '美团科技有限公司', position: '后端开发实习生', applyDate: '2026-06-12', status: '已驳回', department: '计算机科学与技术学院' },
  { id: 5, studentName: '陈同学', studentNo: '20231022', type: '认知实习', company: '阿里巴巴集团', position: '技术参观', applyDate: '2026-06-10', status: '已通过', department: '电子信息工程学院' },
  { id: 6, studentName: '孙同学', studentNo: '20231005', type: '毕业实习', company: '网易集团', position: '游戏策划实习生', applyDate: '2026-06-08', status: '待审核', department: '计算机科学与技术学院' },
  { id: 7, studentName: '周同学', studentNo: '20231011', type: '社会实践', company: '无锡XX科技有限公司', position: 'UI/UX设计实习生', applyDate: '2026-06-05', status: '待审核', department: '设计学院' },
]

export const applicationStatusMap = {
  '待审核': { color: '#F59E0B', bg: 'rgba(245,158,11,0.1)' },
  '已通过': { color: '#16A34A', bg: 'rgba(20,141,61,0.1)' },
  '已驳回': { color: '#FF383C', bg: 'rgba(255,56,60,0.1)' },
}

// ========== TUT-RPT-01 周报月报批阅 ==========

export const reportReviewList = [
  { id: 1, studentName: '张同学', studentNo: '20231001', type: '周报', title: '第3周实习周报', submitDate: '2026-06-22', status: '待批阅', wordCount: 1250 },
  { id: 2, studentName: '王同学', studentNo: '20231002', type: '周报', title: '第3周实习周报', submitDate: '2026-06-21', status: '待批阅', wordCount: 980 },
  { id: 3, studentName: '李同学', studentNo: '20231018', type: '月报', title: '6月实习月报', submitDate: '2026-06-25', status: '待批阅', wordCount: 2100 },
  { id: 4, studentName: '赵同学', studentNo: '20231015', type: '周报', title: '第2周实习周报', submitDate: '2026-06-15', status: '已批阅', wordCount: 1350 },
  { id: 5, studentName: '刘同学', studentNo: '20231008', type: '月报', title: '6月实习月报', submitDate: '2026-06-24', status: '待批阅', wordCount: 1850 },
  { id: 6, studentName: '张同学', studentNo: '20231001', type: '周报', title: '第2周实习周报', submitDate: '2026-06-15', status: '已批阅', wordCount: 1100 },
  { id: 7, studentName: '陈同学', studentNo: '20231022', type: '周报', title: '第1周实习周报', submitDate: '2026-06-08', status: '已批阅', wordCount: 800 },
]

export const reportStatusMap = {
  '待批阅': { color: '#F59E0B', bg: 'rgba(245,158,11,0.1)' },
  '已批阅': { color: '#16A34A', bg: 'rgba(20,141,61,0.1)' },
}

// ========== TUT-EVAL-01 成绩评定 ==========

export const gradeEvaluationList = [
  { id: 1, studentName: '张同学', studentNo: '20231001', type: '毕业实习', company: '华为技术有限公司', position: '软件开发工程师', endDate: '2026-06-20', status: '待评定', schoolScore: '-', enterpriseScore: 90 },
  { id: 2, studentName: '王同学', studentNo: '20231002', type: '毕业实习', company: '腾讯科技有限公司', position: '产品经理助理', endDate: '2026-06-18', status: '待评定', schoolScore: '-', enterpriseScore: 85 },
  { id: 3, studentName: '李同学', studentNo: '20231018', type: '灵活实习', company: '无锡XX科技有限公司', position: '前端开发', endDate: '2026-06-15', status: '待评定', schoolScore: '-', enterpriseScore: 88 },
  { id: 4, studentName: '赵同学', studentNo: '20231015', type: '毕业实习', company: '字节跳动科技有限公司', position: '数据分析', endDate: '2026-06-10', status: '已评定', schoolScore: 85, enterpriseScore: 90 },
  { id: 5, studentName: '刘同学', studentNo: '20231008', type: '毕业实习', company: '美团科技有限公司', position: '后端开发', endDate: '2026-06-08', status: '已评定', schoolScore: 78, enterpriseScore: 82 },
]

export const gradeStatusMap = {
  '待评定': { color: '#F59E0B', bg: 'rgba(245,158,11,0.1)' },
  '已评定': { color: '#16A34A', bg: 'rgba(20,141,61,0.1)' },
}

// 成绩评定维度
export const evaluationDimensions = [
  { name: '实习态度与纪律', weight: 20, maxScore: 100 },
  { name: '专业技能与学习能力', weight: 25, maxScore: 100 },
  { name: '工作成果与质量', weight: 25, maxScore: 100 },
  { name: '沟通协作与团队精神', weight: 15, maxScore: 100 },
  { name: '创新与解决问题能力', weight: 15, maxScore: 100 },
]

// ========== TUT-INTRN-01 指导学生列表 ==========

export const tutStudentList = [
  {
    id: 1, studentName: '张同学', studentNo: '20231001', class: '计科2301', type: '毕业实习',
    company: '华为技术有限公司', position: '软件开发工程师（前端方向）',
    startDate: '2026-03-20', endDate: '2026-06-20', status: '进行中',
    weeklyReports: 3, monthlyReports: 1, alertLevel: 'normal',
    milestones: [
      { label: '提交申请', date: '2026-03-15', done: true },
      { label: '指导老师审核', date: '2026-03-18', done: true },
      { label: '学院复核', date: '2026-03-20', done: true },
      { label: '实习进行中', date: '2026-03-20', done: true, current: true },
      { label: '中期检查', date: '2026-05-15', done: true },
      { label: '实习结束', date: '2026-06-20', done: false },
      { label: '成绩评定', date: '2026-06-25', done: false },
      { label: '归档完成', date: '2026-07-01', done: false },
    ],
  },
  {
    id: 2, studentName: '王同学', studentNo: '20231002', class: '计科2301', type: '毕业实习',
    company: '腾讯科技有限公司', position: '产品经理助理',
    startDate: '2026-03-18', endDate: '2026-06-18', status: '进行中',
    weeklyReports: 2, monthlyReports: 1, alertLevel: 'warning',
    milestones: [
      { label: '提交申请', date: '2026-03-12', done: true },
      { label: '指导老师审核', date: '2026-03-15', done: true },
      { label: '学院复核', date: '2026-03-18', done: true },
      { label: '实习进行中', date: '2026-03-18', done: true, current: true },
      { label: '中期检查', date: '2026-05-15', done: false },
      { label: '实习结束', date: '2026-06-18', done: false },
      { label: '成绩评定', date: '2026-06-22', done: false },
      { label: '归档完成', date: '2026-06-30', done: false },
    ],
  },
  {
    id: 3, studentName: '赵同学', studentNo: '20231015', class: '数学2301', type: '灵活实习',
    company: '字节跳动科技有限公司', position: '数据分析实习生',
    startDate: '2026-04-01', endDate: '2026-07-01', status: '进行中',
    weeklyReports: 4, monthlyReports: 2, alertLevel: 'normal',
    milestones: [
      { label: '提交申请', date: '2026-03-25', done: true },
      { label: '指导老师审核', date: '2026-03-28', done: true },
      { label: '学院复核', date: '2026-03-30', done: true },
      { label: '实习进行中', date: '2026-04-01', done: true, current: true },
      { label: '中期检查', date: '2026-05-15', done: true },
      { label: '实习结束', date: '2026-07-01', done: false },
      { label: '成绩评定', date: '2026-07-05', done: false },
      { label: '归档完成', date: '2026-07-10', done: false },
    ],
  },
  {
    id: 4, studentName: '刘同学', studentNo: '20231008', class: '计科2301', type: '毕业实习',
    company: '美团科技有限公司', position: '后端开发实习生',
    startDate: '2026-03-10', endDate: '2026-06-10', status: '已完成',
    weeklyReports: 5, monthlyReports: 3, alertLevel: 'normal',
    milestones: [
      { label: '提交申请', date: '2026-03-05', done: true },
      { label: '指导老师审核', date: '2026-03-08', done: true },
      { label: '学院复核', date: '2026-03-10', done: true },
      { label: '实习进行中', date: '2026-03-10', done: true },
      { label: '中期检查', date: '2026-05-01', done: true },
      { label: '实习结束', date: '2026-06-10', done: true },
      { label: '成绩评定', date: '2026-06-15', done: true },
      { label: '归档完成', date: '2026-06-20', done: true },
    ],
  },
  {
    id: 5, studentName: '陈同学', studentNo: '20231022', class: '电子2301', type: '认知实习',
    company: '阿里巴巴集团', position: '技术参观',
    startDate: '2026-05-10', endDate: '2026-05-17', status: '已完成',
    weeklyReports: 1, monthlyReports: 0, alertLevel: 'normal',
    milestones: [
      { label: '提交申请', date: '2026-05-01', done: true },
      { label: '指导老师审核', date: '2026-05-05', done: true },
      { label: '学院复核', date: '2026-05-08', done: true },
      { label: '认知实习', date: '2026-05-10', done: true },
      { label: '实习结束', date: '2026-05-17', done: true },
      { label: '总结提交', date: '2026-05-20', done: true },
      { label: '成绩评定', date: '2026-05-25', done: true },
      { label: '归档完成', date: '2026-05-30', done: true },
    ],
  },
  {
    id: 6, studentName: '孙同学', studentNo: '20231005', class: '计科2302', type: '毕业实习',
    company: '网易集团', position: '游戏策划实习生',
    startDate: '2026-06-01', endDate: '2026-08-31', status: '进行中',
    weeklyReports: 2, monthlyReports: 0, alertLevel: 'warning',
    milestones: [
      { label: '提交申请', date: '2026-05-20', done: true },
      { label: '指导老师审核', date: '2026-05-25', done: true },
      { label: '学院复核', date: '2026-05-28', done: true },
      { label: '实习进行中', date: '2026-06-01', done: true, current: true },
      { label: '中期检查', date: '2026-07-15', done: false },
      { label: '实习结束', date: '2026-08-31', done: false },
      { label: '成绩评定', date: '2026-09-05', done: false },
      { label: '归档完成', date: '2026-09-10', done: false },
    ],
  },
]

export const tutStudentStatusMap = {
  '进行中': { color: '#2563EB', bg: 'rgba(37,99,235,0.1)' },
  '已完成': { color: '#16A34A', bg: 'rgba(20,141,61,0.1)' },
  '已结束': { color: '#6B7280', bg: 'rgba(0,0,0,0.05)' },
}

export const alertLevelMap = {
  normal: { label: '正常', color: '#16A34A', bg: 'rgba(20,141,61,0.1)' },
  warning: { label: '关注', color: '#F59E0B', bg: 'rgba(245,158,11,0.1)' },
  danger: { label: '异常', color: '#FF383C', bg: 'rgba(255,56,60,0.1)' },
}
