// 系统管理端路由（P2 优先级）
export default [
  {
    path: '/admin/dashboard',
    name: 'SYS-DASH-01',
    component: () => import('../views/sys/Dashboard.vue'),
    meta: { title: '系统管理工作台' },
  },
  {
    path: '/admin/users',
    name: 'SYS-USER-01',
    component: () => import('../views/sys/UserManage.vue'),
    meta: { title: '用户管理' },
  },
  {
    path: '/admin/roles',
    name: 'SYS-ROLE-01',
    component: () => import('../views/sys/RoleConfig.vue'),
    meta: { title: '角色配置' },
  },
  {
    path: '/admin/settings',
    name: 'SYS-CONFIG-01',
    component: () => import('../views/sys/GlobalConfig.vue'),
    meta: { title: '全局配置' },
  },
  {
    path: '/admin/logs',
    name: 'SYS-LOG-01',
    component: () => import('../views/sys/SystemLogs.vue'),
    meta: { title: '系统日志' },
  },
  {
    path: '/admin/errors',
    name: 'SYS-ERROR-01',
    component: () => import('../views/sys/ErrorHandle.vue'),
    meta: { title: '异常处理' },
  },
]
