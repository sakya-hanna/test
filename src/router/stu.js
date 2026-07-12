export default [
  {
    path: '/stu/dashboard',
    name: 'STU-DASH-01',
    component: () => import('../views/stu/Dashboard.vue'),
    meta: { title: '学生工作台', menu: '工作台' },
  },
  {
    path: '/stu/internships/overview',
    name: 'STU-INTRN-02',
    component: () => import('../views/stu/InternshipOverview.vue'),
    meta: { title: '我的实习总览', menu: '我的实习' },
  },
  {
    path: '/stu/internships',
    name: 'STU-INTRN-01',
    component: () => import('../views/stu/InternshipList.vue'),
    meta: { title: '我的实习列表', menu: '我的实习' },
  },
  {
    path: '/stu/internships/new',
    name: 'STU-APP-01',
    component: () => import('../views/stu/InternshipApplication.vue'),
    meta: { title: '实习申请表', menu: '我的实习' },
  },
  {
    path: '/stu/internships/:internshipId',
    name: 'STU-INTRN-01-D01',
    component: () => import('../views/stu/InternshipDetail.vue'),
    meta: { title: '实习详情', menu: '我的实习' },
  },
  {
    path: '/stu/jobs',
    name: 'STU-JOB-01',
    component: () => import('../views/stu/JobList.vue'),
    meta: { title: '企业需求浏览', menu: '企业需求' },
  },
  {
    path: '/stu/evaluations',
    name: 'STU-EVAL-01',
    component: () => import('../views/stu/EvaluationList.vue'),
    meta: { title: '成绩查看', menu: '成绩查看' },
  },
  {
    path: '/stu/messages',
    name: 'COM-NOTI-01',
    component: () => import('../views/stu/Messages.vue'),
    meta: { title: '通知公告', menu: '通知公告' },
  },
  {
    path: '/stu/reports/weekly/new',
    name: 'STU-RPT-01',
    component: () => import('../views/stu/WeeklyReport.vue'),
    meta: { title: '实习周报提交', menu: '过程记录' },
  },
  {
    path: '/stu/reports/monthly/new',
    name: 'STU-RPT-02',
    component: () => import('../views/stu/MonthlyReport.vue'),
    meta: { title: '实习月报提交', menu: '过程记录' },
  },
  {
    path: '/stu/reports',
    name: 'STU-RPT-00',
    component: () => import('../views/stu/Reports.vue'),
    meta: { title: '过程记录', menu: '过程记录' },
  },
  {
    path: '/stu/materials',
    name: 'STU-RPT-03-MAT',
    component: () => import('../views/stu/Materials.vue'),
    meta: { title: '材料提交', menu: '材料提交' },
  },
  {
    path: '/stu/achievements/new',
    name: 'STU-RPT-03',
    component: () => import('../views/stu/AchievementSubmit.vue'),
    meta: { title: '实习成果提交', menu: '材料提交' },
  },
  {
    path: '/stu/parent-notice',
    name: 'STU-PAR-01',
    component: () => import('../views/stu/ParentNotice.vue'),
    meta: { title: '家长知情', menu: '家长知情' },
  },
  // TODO: 其他学生端路由待补充
]
