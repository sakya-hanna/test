import { createRouter, createWebHistory } from 'vue-router'
import stuRoutes from './stu.js'
import tutRoutes from './tut.js'
import schRoutes from './sch.js'
import colRoutes from './col.js'
import authRoutes from './auth.js'
import entRoutes from './ent.js'
import couRoutes from './cou.js'
import sysRoutes from './sys.js'

const routes = [
  // 登录认证
  ...authRoutes,

  // 各角色端
  ...stuRoutes,
  ...tutRoutes,
  ...schRoutes,
  ...colRoutes,
  ...entRoutes,
  ...couRoutes,

  // 系统管理（P2）
  ...sysRoutes,

  // TODO: 待确认 — 公共通知页面 Notifications.vue / NotificationDetail.vue 当前路由归属和入口
  { path: '/notifications', name: 'Notifications', component: () => import('../views/Notifications.vue') },
  { path: '/notifications/:id', name: 'NotificationDetail', component: () => import('../views/NotificationDetail.vue') },

  // 测试页面
  { path: '/test-demo', name: 'TestDemo', component: () => import('../views/TestDemo.vue') },
  { path: '/hello', name: 'HelloWorld', component: () => import('../views/HelloWorld.vue') },

  // 兜底
  { path: '/', redirect: '/login' },
  { path: '/:pathMatch(.*)*', name: 'NotFound', component: () => import('../views/NotFound.vue') },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
