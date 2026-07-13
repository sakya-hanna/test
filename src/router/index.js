import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Hello',
    component: () => import('../views/HelloWorld.vue'),
  },
  {
    path: '/goodbye',
    name: 'Goodbye',
    component: () => import('../views/GoodbyeWorld.vue'),
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
