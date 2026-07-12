// ========== COL-AUD-02 学生实习申请复核 ==========

export const colApplicationReviewList = [
  { id: 1, studentName: '张同学', studentNo: '20231001', class: '计科2301', type: '毕业实习', company: '华为技术有限公司', position: '软件开发工程师（前端方向）', applyDate: '2026-06-20', teacherAudit: '已通过', teacherOpinion: '申请材料完整，实习岗位与专业相关度高，建议通过。', status: '待复核' },
  { id: 2, studentName: '王同学', studentNo: '20231002', class: '计科2301', type: '毕业实习', company: '腾讯科技有限公司', position: '产品经理助理', applyDate: '2026-06-18', teacherAudit: '已通过', teacherOpinion: '学生综合素质良好，岗位匹配度较高。', status: '待复核' },
  { id: 3, studentName: '赵同学', studentNo: '20231015', class: '数学2301', type: '灵活实习', company: '字节跳动科技有限公司', position: '数据分析实习生', applyDate: '2026-06-15', teacherAudit: '已通过', teacherOpinion: '数据分析岗位与数学专业吻合，同意申请。', status: '已通过' },
  { id: 4, studentName: '刘同学', studentNo: '20231008', class: '计科2301', type: '毕业实习', company: '美团科技有限公司', position: '后端开发实习生', applyDate: '2026-06-12', teacherAudit: '已驳回', teacherOpinion: '实习岗位与培养方案不完全匹配，建议调整。', status: '待复核' },
  { id: 5, studentName: '陈同学', studentNo: '20231022', class: '电子2301', type: '认知实习', company: '阿里巴巴集团', position: '技术参观', applyDate: '2026-06-10', teacherAudit: '已通过', teacherOpinion: '认知实习安排合理，时间与课程不冲突。', status: '已通过' },
  { id: 6, studentName: '孙同学', studentNo: '20231005', class: '计科2302', type: '毕业实习', company: '网易集团', position: '游戏策划实习生', applyDate: '2026-06-08', teacherAudit: '已通过', teacherOpinion: '行业匹配，实习内容充实，建议通过。', status: '待复核' },
]

export const colMessages = [
  { id: 1, title: '张同学的实习申请待复核', from: '审核提醒', time: '2026-06-23 14:00', read: false, content: '学生张同学（20231001）的毕业实习申请已通过指导老师初审，请进行学院级复核。企业：华为技术有限公司，岗位：软件开发工程师（前端方向）。' },
  { id: 2, title: '企业入驻审核规范更新', from: '系统通知', time: '2026-06-20 09:00', read: true, content: '学校已更新企业入驻审核流程规范，学院初审通过后自动流转至学校复核，无需手动转交。请各学院管理员知悉。' },
  { id: 3, title: '6月实习数据汇总通知', from: '教务处', time: '2026-06-18 16:00', read: true, content: '请各学院于6月28日前汇总并上报本学院6月实习数据，包括实习生人数、企业数量、审核通过率等指标。' },
  { id: 4, title: '孙同学的审核申请需关注', from: '审核提醒', time: '2026-06-22 10:00', read: false, content: '学生孙同学（20231005）的申请指导老师初审为"已通过"，但其初审意见中标注了岗位匹配度问题，请复核时特别关注。' },
]

export const colAppStatusMap = {
  '待复核': { color: '#F59E0B', bg: 'rgba(245,158,11,0.1)' },
  '已通过': { color: '#16A34A', bg: 'rgba(20,141,61,0.1)' },
  '已驳回': { color: '#FF383C', bg: 'rgba(255,56,60,0.1)' },
}

// ========== COL-DASH-01 学院管理工作台 ==========

export const colDashboardStats = {
  statCards: [
    { label: '待审核企业', value: 5, color: '#2563EB' },
    { label: '待复核申请', value: 12, color: '#F59E0B' },
    { label: '本月实习生', value: 86, color: '#16A34A' },
    { label: '通知公告', value: 3, color: '#7C3AED' },
  ],
  pendingItems: [
    { id: 1, text: '华为技术有限公司 — 企业入驻待初审', type: '审核', link: '/college/companies/review' },
    { id: 2, text: '张同学（计科2301）— 实习申请待复核', type: '复核', link: '/college/applications/review' },
    { id: 3, text: '腾讯科技有限公司 — 企业信息变更待审核', type: '审核', link: '/college/companies/review' },
    { id: 4, text: '孙同学（计科2302）— 实习申请待复核', type: '复核', link: '/college/applications/review' },
  ],
  recentNotices: [
    { id: 1, title: '企业入驻审核规范更新', from: '教务处', time: '06-23' },
    { id: 2, title: '6月实习数据汇总通知', from: '教务处', time: '06-18' },
    { id: 3, title: '关于规范实习申请审核流程的通知', from: '学校管理', time: '06-15' },
  ],
}

// ========== COL-AUD-01 企业入驻审核（学院初审） ==========

export const colCompanyReviewList = [
  { id: 1, name: '华为技术有限公司', industry: '信息技术', scale: '10000人以上', contact: '张经理', phone: '138****6789', applyDate: '2026-06-20', status: '待初审' },
  { id: 2, name: '腾讯科技有限公司', industry: '互联网', scale: '10000人以上', contact: '李总', phone: '139****8901', applyDate: '2026-06-18', status: '待初审' },
  { id: 3, name: '字节跳动科技有限公司', industry: '信息技术', scale: '5000-10000人', contact: '王HR', phone: '137****3456', applyDate: '2026-06-15', status: '已通过' },
  { id: 4, name: '美团科技有限公司', industry: '互联网', scale: '5000-10000人', contact: '赵经理', phone: '136****7890', applyDate: '2026-06-12', status: '待初审' },
  { id: 5, name: '网易集团', industry: '互联网', scale: '3000-5000人', contact: '孙总', phone: '135****2345', applyDate: '2026-06-10', status: '已驳回' },
  { id: 6, name: '小米科技有限公司', industry: '电子制造', scale: '5000-10000人', contact: '刘HR', phone: '133****6789', applyDate: '2026-06-08', status: '已通过' },
]

export const colCompanyStatusMap = {
  '待初审': { color: '#F59E0B', bg: 'rgba(245,158,11,0.1)' },
  '已通过': { color: '#16A34A', bg: 'rgba(20,141,61,0.1)' },
  '已驳回': { color: '#FF383C', bg: 'rgba(255,56,60,0.1)' },
}
