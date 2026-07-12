export default [
  {
    path: '/college/dashboard',
    name: 'COL-DASH-01',
    component: () => import('../views/col/Dashboard.vue'),
    meta: { title: '学院管理工作台', menu: '工作台' },
  },
  {
    path: '/college/companies/review',
    name: 'COL-AUD-01',
    component: () => import('../views/col/CompanyReview.vue'),
    meta: { title: '企业入驻审核（学院初审）', menu: '企业入驻审核' },
  },
  {
    path: '/college/applications/review',
    name: 'COL-AUD-02',
    component: () => import('../views/col/ApplicationReview.vue'),
    meta: { title: '学生实习申请复核', menu: '学生申请复核' },
  },
  {
    path: '/college/notifications',
    name: 'COL-NOTI-01',
    component: () => import('../views/col/NotificationManage.vue'),
    meta: { title: '通知公告发布', menu: '通知公告' },
  },
  {
    path: '/college/messages',
    name: 'COL-NOTI-02',
    component: () => import('../views/col/Messages.vue'),
    meta: { title: '消息中心', menu: '消息中心' },
  },
]
