export default [
  {
    path: '/school/dashboard',
    name: 'SCH-DASH-01',
    component: () => import('../views/sch/Dashboard.vue'),
    meta: { title: '学校管理工作台', menu: '工作台' },
  },
  {
    path: '/school/companies/review',
    name: 'SCH-AUD-01',
    component: () => import('../views/sch/CompanyReview.vue'),
    meta: { title: '企业入驻复核', menu: '企业管理' },
  },
  {
    path: '/school/internship-plans',
    name: 'SCH-INTRN-01',
    component: () => import('../views/sch/InternshipPlan.vue'),
    meta: { title: '实习计划发布', menu: '实习管理' },
  },
  {
    path: '/school/notifications',
    name: 'SCH-NOTI-01',
    component: () => import('../views/sch/NotificationManage.vue'),
    meta: { title: '通知公告管理', menu: '通知公告' },
  },
  {
    path: '/school/analytics',
    name: 'SCH-DASH-02',
    component: () => import('../views/sch/Analytics.vue'),
    meta: { title: '统计分析看板', menu: '统计分析' },
  },
  {
    path: '/school/messages',
    name: 'SCH-NOTI-02',
    component: () => import('../views/sch/Messages.vue'),
    meta: { title: '消息中心', menu: '消息中心' },
  },
]
