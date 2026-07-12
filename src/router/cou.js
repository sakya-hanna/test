export default [
  {
    path: '/counselor/dashboard',
    name: 'COU-DASH-01',
    component: () => import('../views/cou/Dashboard.vue'),
    meta: { title: '辅导员工作台', menu: '工作台' },
  },
  {
    path: '/counselor/student-list',
    name: 'COU-STU-01',
    component: () => import('../views/cou/StudentList.vue'),
    meta: { title: '学生列表', menu: '学生列表' },
  },
  {
    path: '/counselor/students',
    name: 'COU-TRACK-01',
    component: () => import('../views/cou/StudentTrack.vue'),
    meta: { title: '学生实习跟踪', menu: '学生追踪' },
  },
  {
    path: '/counselor/alerts',
    name: 'COU-ALERT-01',
    component: () => import('../views/cou/SafetyAlert.vue'),
    meta: { title: '安全预警', menu: '安全预警' },
  },
  {
    path: '/counselor/parent-notice',
    name: 'COU-PAR-01',
    component: () => import('../views/cou/ParentNotice.vue'),
    meta: { title: '家长知情确认', menu: '家长知情' },
  },
  {
    path: '/counselor/process-feedback',
    name: 'COU-FB-01',
    component: () => import('../views/cou/ProcessFeedback.vue'),
    meta: { title: '过程反馈', menu: '过程反馈' },
  },
  {
    path: '/counselor/evaluations',
    name: 'COU-EVAL-01',
    component: () => import('../views/cou/StudentEvaluation.vue'),
    meta: { title: '学生评价', menu: '学生评价' },
  },
  {
    path: '/counselor/notifications',
    name: 'COU-NOTI-01',
    component: () => import('../views/cou/Notifications.vue'),
    // TODO: 待确认 — 辅导员通知页面 ID 未在资产台账中确认
    meta: { title: '通知公告' },
  },
]
