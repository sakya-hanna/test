// ============================================================
// 辅导员端 Mock 数据
// ============================================================

// --- COU-DASH-01 辅导员工作台 ---
export const couStats = [
  { label: '所带学生', value: '156 人', color: '#2563EB' },
  { label: '实习中', value: '128 人', color: '#16A34A' },
  { label: '异常预警', value: '8 人', color: '#FF383C' },
  { label: '待沟通', value: '3 人', color: '#D97706' },
]

export const couAlerts = [
  { id: 1, name: '李明  软件2201', reason: '连续2周未提交周报', lv: '紧急', lvBg: 'rgba(255,56,60,0.15)', lvColor: '#FF383C' },
  { id: 2, name: '王芳  软件2202', reason: '实习企业变更未报备', lv: '重要', lvBg: 'rgba(217,119,6,0.15)', lvColor: '#D97706' },
  { id: 3, name: '赵强  计算机2201', reason: '本周日报未提交', lv: '提醒', lvBg: 'rgba(37,99,235,0.15)', lvColor: '#2563EB' },
  { id: 4, name: '陈雪  计算机2202', reason: '实习单位反馈异常', lv: '重要', lvBg: 'rgba(217,119,6,0.15)', lvColor: '#D97706' },
]

export const couOverviewList = [
  { t: '毕业实习', n: 86, s: '进行中', c: '#2563EB', sBg: 'rgba(37,99,235,0.1)' },
  { t: '企业需求对接', n: 24, s: '进行中', c: '#16A34A', sBg: 'rgba(22,163,74,0.1)' },
  { t: '灵活实习', n: 12, s: '进行中', c: '#D97706', sBg: 'rgba(217,119,6,0.1)' },
  { t: '认知实习', n: 8, s: '已完成', c: '#6B7280', sBg: 'rgba(107,114,128,0.1)' },
  { t: '暑期社会实践', n: 16, s: '未开始', c: '#9CA3AF', sBg: 'rgba(156,163,175,0.1)' },
]

export const couCommunications = [
  { id: 1, name: '李明', date: '2026-03-20', m: '电话沟通', note: '提醒周报提交' },
  { id: 2, name: '王芳', date: '2026-03-19', m: '微信沟通', note: '确认企业变更情况' },
  { id: 3, name: '赵强', date: '2026-03-18', m: '家长沟通', note: '反馈学生实习情况' },
]

// --- COU-INTRN-01 学生实习跟踪 ---
export const couStudentList = [
  { id: 1, name: '李明', sid: '2024030112', cls: '软件2201', company: '无锡科技有限公司', type: '毕业实习', progress: '3/12', lastSubmit: '03-20 周报', status: '正常', statusBg: 'rgba(22,163,74,0.1)', statusColor: '#16A34A', action: '详情', actionColor: '#2563EB' },
  { id: 2, name: '王芳', sid: '2024030115', cls: '软件2202', company: '苏州创新技术公司', type: '企业需求对接', progress: '2/12', lastSubmit: '03-18 月报', status: '正常', statusBg: 'rgba(22,163,74,0.1)', statusColor: '#16A34A', action: '详情', actionColor: '#2563EB' },
  { id: 3, name: '赵强', sid: '2024030120', cls: '计算机2201', company: '南京软件有限公司', type: '毕业实习', progress: '1/12', lastSubmit: '03-10 周报', status: '异常', statusBg: 'rgba(255,56,60,0.1)', statusColor: '#FF383C', action: '详情', actionColor: '#2563EB' },
  { id: 4, name: '陈雪', sid: '2024030125', cls: '计算机2202', company: '上海数据集团', type: '灵活实习', progress: '0/12', lastSubmit: '未提交', status: '未提交', statusBg: 'rgba(217,119,6,0.1)', statusColor: '#D97706', action: '提醒', actionColor: '#FF383C' },
  { id: 5, name: '刘洋', sid: '2024030130', cls: '软件2201', company: '杭州云科技有限公司', type: '认知实习', progress: '4/4', lastSubmit: '03-15 报告', status: '已完成', statusBg: 'rgba(107,114,128,0.1)', statusColor: '#6B7280', action: '详情', actionColor: '#2563EB' },
  { id: 6, name: '周婷', sid: '2024030135', cls: '计算机2201', company: '常州智能装备公司', type: '毕业实习', progress: '3/12', lastSubmit: '03-17 周报', status: '正常', statusBg: 'rgba(22,163,74,0.1)', statusColor: '#16A34A', action: '详情', actionColor: '#2563EB' },
]

// --- COU-NOTI-01 安全提醒沟通 ---
export const couSafetyStudents = [
  { id: 1, name: '李明', cls: '软件2201', parentPhone: '138-0000-1234', status: '正常', statusBg: 'rgba(22,163,74,0.1)', statusColor: '#16A34A' },
  { id: 2, name: '赵强', cls: '计算机2201', parentPhone: '139-0000-5678', status: '异常', statusBg: 'rgba(255,56,60,0.1)', statusColor: '#FF383C' },
  { id: 3, name: '陈雪', cls: '计算机2202', parentPhone: '137-0000-9012', status: '未提交', statusBg: 'rgba(217,119,6,0.1)', statusColor: '#D97706' },
]

export const couDefaultMsg = '各位家长您好：\n近期实习工作进入关键阶段，请提醒学生注意通勤安全，\n严格遵守实习单位的安全管理规定。如有异常请及时联系。\n\n—— 计算机学院辅导员'

export const couCommHistory = [
  { time: '03-20 14:30', student: '李明', target: '家长(母亲)', method: '电话', summary: '提醒实习通勤安全，确认学生每日打卡', result: '家长已确认' },
  { time: '03-19 10:00', student: '赵强', target: '学生本人', method: '微信', summary: '询问本周未提交周报原因，督促尽快补交', result: '学生承诺补交' },
  { time: '03-18 16:00', student: '陈雪', target: '家长(父亲)', method: '短信', summary: '告知学生实习单位异常情况，建议家校沟通', result: '家长表示关注' },
  { time: '03-15 09:30', student: '王芳', target: '家长(母亲)', method: '电话', summary: '通报学生近期实习表现良好，提醒继续保持', result: '沟通顺利' },
]

// --- 辅导员消息中心 ---
export const couMessages = [
  { id: 1, title: '学生异常预警通知', type: 'alert', from: '系统监测', time: '2026-06-19 09:30', read: false, content: '赵强同学（计算机2201班）连续2周未提交周报，系统自动标记为异常状态。\n\n学生信息：赵强 | 学号 2024030120 | 计算机2201班\n实习类型：毕业实习 | 南京软件有限公司\n未提交周期：第3周、第4周\n\n建议操作：\n1. 立即联系学生了解情况\n2. 必要时联系家长沟通\n3. 在「学生实习跟踪」页面记录处理结果' },
  { id: 2, title: '实习安全管理通知', type: 'notice', from: '学校管理员', time: '2026-06-18 15:00', read: false, content: '请各辅导员加强对学生实习安全的管理和提醒，确保学生按时提交安全打卡。\n\n近期工作要求：\n1. 确认所有实习学生每周至少完成1次安全打卡\n2. 对连续2周未打卡学生进行电话沟通\n3. 在「安全提醒沟通」页面记录沟通情况\n4. 如发现异常情况及时上报学院学工办' },
  { id: 3, title: '企业导师反馈通知', type: 'report', from: '企业导师', time: '2026-06-17 11:20', read: true, content: '无锡科技有限公司企业导师陈志远反馈：李明同学的实习表现优秀，工作态度认真，技术能力突出，建议给予表彰。\n\n学生：李明 | 软件2201班\n企业：无锡科技有限公司\n岗位：Java开发实习生\n导师评价：该生积极主动，学习能力强，能够独立完成任务\n\n请关注该生的实习进展，可作为优秀实习生候选人推荐。' },
  { id: 4, title: '周报批阅提醒', type: 'report', from: '系统通知', time: '2026-06-15 08:00', read: true, content: '本周有3名学生周报待批阅，请及时完成批阅工作。\n\n待批阅学生：\n1. 王芳 - 第5周周报 - 企业需求对接\n2. 陈雪 - 第5周周报 - 灵活实习\n3. 刘洋 - 第4周报告 - 认知实习\n\n请前往教师端「周报月报批阅」页面完成批阅，截止日期为本周五。' },
  { id: 5, title: '学生实习变更通知', type: 'alert', from: '系统通知', time: '2026-06-14 10:00', read: true, content: '学生陈雪（计算机2202班）提交了实习单位变更申请，请进行审核确认。\n\n变更详情：\n原实习单位：上海数据集团有限公司\n新实习单位：常州智能装备有限公司\n变更原因：原单位项目结束，转入新单位继续实习\n\n请在「学生实习跟踪」页面查看详情并确认变更。' },
]

// --- 辅导员侧边栏菜单 ---
export const couMenuItems = [
  { to: '/counselor/dashboard', label: '指导工作台' },
  { to: '/counselor/students', label: '学生实习跟踪' },
  { to: '/counselor/alerts', label: '安全提醒沟通' },
  { to: '/counselor/notifications', label: '消息中心' },
]
