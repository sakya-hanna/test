// ========== SCH-DASH-01 学校管理工作台 ==========

export const schDashboardStats = {
  statCards: [
    { label: '入驻企业', value: 48, color: '#2563EB' },
    { label: '在籍实习生', value: 1256, color: '#16A34A' },
    { label: '待复核企业', value: 7, color: '#F59E0B' },
    { label: '本月新增', value: 12, color: '#7C3AED' },
  ],
  pendingItems: [
    { id: 1, text: '复核无锡XX科技企业入驻申请', type: '复核', link: '/school/companies/review' },
    { id: 2, text: '发布2026暑期实习计划', type: '发布', link: '/school/internship-plans' },
    { id: 3, text: '审核企业资质变更申请', type: '审核', link: '/school/companies/review' },
    { id: 4, text: '发布毕业实习材料提交通知', type: '通知', link: '/school/notifications' },
  ],
  recentNotices: [
    { id: 1, title: '省教育厅关于开展实习管理专项检查的通知', time: '2026-06-20', from: '省教育厅' },
    { id: 2, title: '2026届毕业实习数据统计报告', time: '2026-06-18', from: '教务处' },
    { id: 3, title: '关于规范企业入驻审核流程的通知', time: '2026-06-15', from: '实习管理科' },
  ],
  messages: [
    { id: 1, title: '无锡XX科技企业入驻待复核', from: '审核提醒', time: '2026-06-23 15:30', read: false, content: '无锡XX科技有限公司已通过学院初审，请尽快进行学校级入驻复核。企业信息：信息技术行业，50-200人规模。' },
    { id: 2, title: '省教育厅实习管理专项检查通知', from: '省教育厅', time: '2026-06-22 08:00', read: false, content: '省教育厅将于7月初开展全省高校实习管理专项检查，请各校做好迎检准备，整理实习管理台账、自查报告等材料。' },
    { id: 3, title: '2026届毕业实习数据统计完成', from: '系统通知', time: '2026-06-20 16:00', read: true, content: '2026届毕业实习数据统计已完成，共1256名实习生、48家入驻企业。详细数据请查看统计分析看板。' },
    { id: 4, title: '暑期社会实践计划已发布', from: '系统通知', time: '2026-06-18 10:00', read: true, content: '2026年暑期社会实践计划已成功发布，目标为2023级/2024级全校学生，共620人。起止时间：2026-07-01 ~ 2026-08-31。' },
  ],
  quickActions: [
    { label: '企业复核', icon: '🏢', link: '/school/companies/review' },
    { label: '实习计划', icon: '📋', link: '/school/internship-plans' },
    { label: '发布通知', icon: '📢', link: '/school/notifications' },
    { label: '统计分析', icon: '📊', link: '/school/analytics' },
  ],
}

// ========== SCH-AUD-01 企业入驻复核 ==========

export const companyReviewList = [
  { id: 1, name: '无锡XX科技有限公司', creditCode: '91320200MA1XXXXXX1', industry: '信息技术', scale: '50-200人', contact: '张经理', contactPhone: '138****1234', applyDate: '2026-06-20', collegeAudit: '已通过', status: '待复核' },
  { id: 2, name: '南京智慧云科技有限公司', creditCode: '91320100MA1XXXXXX2', industry: '软件服务', scale: '200-500人', contact: '李总监', contactPhone: '139****5678', applyDate: '2026-06-18', collegeAudit: '已通过', status: '待复核' },
  { id: 3, name: '苏州工业园区创新中心', creditCode: '91320500MA1XXXXXX3', industry: '科技服务', scale: '100-200人', contact: '王主任', contactPhone: '137****9012', applyDate: '2026-06-15', collegeAudit: '已通过', status: '待复核' },
  { id: 4, name: '杭州数字工厂有限公司', creditCode: '91330100MA1XXXXXX4', industry: '智能制造', scale: '500-1000人', contact: '赵经理', contactPhone: '136****3456', applyDate: '2026-06-12', collegeAudit: '已通过', status: '已通过' },
  { id: 5, name: '上海云谷信息技术有限公司', creditCode: '91310100MA1XXXXXX5', industry: '信息技术', scale: '1000人以上', contact: '陈总', contactPhone: '135****7890', applyDate: '2026-06-10', collegeAudit: '已通过', status: '已驳回' },
  { id: 6, name: '合肥量子科技有限公司', creditCode: '91340100MA1XXXXXX6', industry: '量子技术', scale: '50人以下', contact: '孙博士', contactPhone: '133****1111', applyDate: '2026-06-08', collegeAudit: '已驳回', status: '待复核' },
]

export const companyStatusMap = {
  '待复核': { color: '#F59E0B', bg: 'rgba(245,158,11,0.1)' },
  '已通过': { color: '#16A34A', bg: 'rgba(20,141,61,0.1)' },
  '已驳回': { color: '#FF383C', bg: 'rgba(255,56,60,0.1)' },
}

// ========== SCH-INTRN-01 实习计划发布 ==========

export const internshipPlanList = [
  { id: 1, title: '2026-2027学年毕业实习计划', type: '毕业实习', targetGrade: '2023级', targetDept: '全校', startDate: '2026-09-01', endDate: '2027-01-15', publishDate: '2026-06-15', status: '已发布', studentCount: 850 },
  { id: 2, title: '2026年暑期社会实践计划', type: '社会实践', targetGrade: '2023级/2024级', targetDept: '全校', startDate: '2026-07-01', endDate: '2026-08-31', publishDate: '2026-06-10', status: '已发布', studentCount: 620 },
  { id: 3, title: '2026年秋季认知实习安排', type: '认知实习', targetGrade: '2024级', targetDept: '计算机学院/电子学院', startDate: '2026-10-15', endDate: '2026-11-15', publishDate: '', status: '草稿', studentCount: 320 },
  { id: 4, title: '2026年灵活实习实施方案', type: '灵活实习', targetGrade: '2023级', targetDept: '全校', startDate: '2026-06-01', endDate: '2027-06-01', publishDate: '2026-05-20', status: '已发布', studentCount: 180 },
]

export const planStatusMap = {
  '已发布': { color: '#16A34A', bg: 'rgba(20,141,61,0.1)' },
  '草稿': { color: '#F59E0B', bg: 'rgba(245,158,11,0.1)' },
  '已归档': { color: '#6B7280', bg: 'rgba(0,0,0,0.05)' },
}

// ========== SCH-NOTI-01 通知公告管理 ==========

export const notificationList = [
  { id: 1, title: '关于开展2026年暑期社会实践的通知', type: '通知公告', channel: '全渠道', audience: '全校师生', publishDate: '2026-06-20', publisher: '王处长', status: '已发布', readCount: 856 },
  { id: 2, title: '关于2026届毕业实习材料提交的提醒', type: '重要提醒', channel: 'App推送+站内信', audience: '2023级学生', publishDate: '2026-06-18', publisher: '李老师', status: '已发布', readCount: 623 },
  { id: 3, title: '关于规范实习企业资质审核的通知', type: '制度文件', channel: '站内信', audience: '各学院管理员', publishDate: '2026-06-15', publisher: '王处长', status: '已发布', readCount: 48 },
  { id: 4, title: '2026年秋季认知实习报名通知', type: '通知公告', channel: '全渠道', audience: '2024级学生', publishDate: '', publisher: '张老师', status: '草稿', readCount: 0 },
  { id: 5, title: '关于实习安全管理的重要通知', type: '重要提醒', channel: '全渠道', audience: '全校师生', publishDate: '2026-06-05', publisher: '王处长', status: '已发布', readCount: 1205 },
]

export const notiStatusMap = {
  '已发布': { color: '#16A34A', bg: 'rgba(20,141,61,0.1)' },
  '草稿': { color: '#F59E0B', bg: 'rgba(245,158,11,0.1)' },
}

export const notiTypes = ['通知公告', '重要提醒', '制度文件', '活动通知']
export const notiChannels = ['全渠道', 'App推送', '站内信', '短信', 'App推送+站内信']

// ========== SCH-DASH-02 统计分析看板 ==========

export const analyticsData = {
  overview: [
    { label: '在籍实习生', value: 1256, change: '+12%', up: true },
    { label: '入驻企业', value: 48, change: '+8%', up: true },
    { label: '本月新增实习', value: 86, change: '-3%', up: false },
    { label: '完成率', value: '78%', change: '+5%', up: true },
  ],
  byType: [
    { name: '毕业实习', count: 680, percent: 54 },
    { name: '灵活实习', count: 280, percent: 22 },
    { name: '认知实习', count: 180, percent: 14 },
    { name: '社会实践', count: 116, percent: 9 },
  ],
  byCollege: [
    { name: '计算机科学与技术学院', count: 320 },
    { name: '电子信息工程学院', count: 245 },
    { name: '数学与统计学院', count: 180 },
    { name: '设计学院', count: 156 },
    { name: '商学院', count: 142 },
    { name: '外国语学院', count: 98 },
    { name: '机械工程学院', count: 115 },
  ],
  monthlyTrend: [
    { month: '1月', count: 45 }, { month: '2月', count: 32 }, { month: '3月', count: 120 },
    { month: '4月', count: 95 }, { month: '5月', count: 156 }, { month: '6月', count: 210 },
  ],
  statusDist: [
    { name: '进行中', count: 580, color: '#2563EB' },
    { name: '已完成', count: 450, color: '#16A34A' },
    { name: '审核中', count: 126, color: '#F59E0B' },
    { name: '已终止', count: 100, color: '#FF383C' },
  ],
}
