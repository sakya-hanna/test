export default [
  {
    path: '/teacher/dashboard',
    name: 'TUT-DASH-01',
    component: () => import('../views/tut/Dashboard.vue'),
    meta: { title: '教师工作台', menu: '教师工作台' },
  },
  {
    path: '/teacher/applications',
    name: 'TUT-AUD-01',
    component: () => import('../views/tut/ApplicationReview.vue'),
    meta: { title: '实习申请审核', menu: '实习申请审核' },
  },
  {
    path: '/teacher/reports',
    name: 'TUT-RPT-01',
    component: () => import('../views/tut/ReportReview.vue'),
    meta: { title: '周报月报批阅', menu: '周报月报批阅' },
  },
  {
    path: '/teacher/grades',
    name: 'TUT-EVAL-01',
    component: () => import('../views/tut/GradeEvaluation.vue'),
    meta: { title: '成绩评定', menu: '成绩评定' },
  },
  {
    path: '/teacher/students',
    name: 'TUT-INTRN-01',
    component: () => import('../views/tut/StudentList.vue'),
    meta: { title: '指导学生列表', menu: '指导学生列表' },
  },
  {
    path: '/teacher/notifications',
    name: 'TUT-NOTI-01',
    component: () => import('../views/tut/Notifications.vue'),
    meta: { title: '消息中心', menu: '消息中心' },
  },
]
