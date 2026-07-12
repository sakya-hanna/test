<script setup>
import { computed, watchEffect } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const roleStorageKey = 'companyDemoRole'

const roleLabels = {
  admin: '企业管理员',
  contact: '企业联系人',
  recruiter: '招聘负责人',
  mentor: '企业指导老师',
}

const menuItems = [
  { label: '企业管理员工作台', key: 'admin-home', path: '/company/admin', roles: ['admin'] },
  { label: '人员账号管理', key: 'accounts', path: '/company/admin/accounts', roles: ['admin'] },
  { label: '招聘负责人工作台', key: 'recruiter-home', path: '/company/recruiter', roles: ['recruiter'] },
  { label: '企业联系人工作台', key: 'contact-home', path: '/company/contact', roles: ['contact'] },
  { label: '企业导师工作台', key: 'mentor-home', path: '/company/mentor', roles: ['mentor'] },
  { label: '企业信息管理', key: 'profile', path: '/company/profile', roles: ['admin'] },
  { label: '实习需求管理', key: 'jobs', path: '/company/jobs', roles: ['recruiter'] },
  { label: '学生申请管理', key: 'applications', path: '/company/applications', roles: ['contact', 'recruiter'] },
  { label: '实习对接管理', key: 'internships', path: '/company/internships', roles: ['contact', 'mentor'] },
  { label: '企业指导老师管理', key: 'mentors', path: '/company/mentors', roles: ['admin'] },
  { label: '学生评价', key: 'evaluation', path: '/company/internships/1/evaluation', roles: ['mentor'] },
  { label: '企业消息中心', key: 'notifications', path: '/company/notifications', roles: ['admin', 'contact', 'recruiter', 'mentor'] },
]

function normalizeRole(role) {
  return roleLabels[role] ? role : ''
}

function getStoredRole() {
  if (typeof window === 'undefined') return ''
  return normalizeRole(window.localStorage.getItem(roleStorageKey))
}

function setStoredRole(role) {
  if (typeof window === 'undefined' || !role) return
  window.localStorage.setItem(roleStorageKey, role)
}

const routeRole = computed(() => {
  const raw = Array.isArray(route.query.role) ? route.query.role[0] : route.query.role
  return normalizeRole(raw)
})

const pathRole = computed(() => {
  if (route.path.startsWith('/company/admin')) return 'admin'
  if (route.path.startsWith('/company/recruiter')) return 'recruiter'
  if (route.path.startsWith('/company/contact')) return 'contact'
  if (route.path.startsWith('/company/mentor') && !route.path.startsWith('/company/mentors')) return 'mentor'
  return ''
})

const currentRole = computed(() => routeRole.value || pathRole.value || getStoredRole() || 'contact')
const currentRoleLabel = computed(() => roleLabels[currentRole.value] || roleLabels.contact)
const visibleMenuItems = computed(() => menuItems.filter(item => item.roles.includes(currentRole.value)))

watchEffect(() => {
  if (routeRole.value || pathRole.value) setStoredRole(currentRole.value)
})

const activeKey = computed(() => {
  if (route.path.startsWith('/company/admin/accounts')) return 'accounts'
  if (route.path.startsWith('/company/admin')) return 'admin-home'
  if (route.path.startsWith('/company/recruiter')) return 'recruiter-home'
  if (route.path.startsWith('/company/contact')) return 'contact-home'
  if (route.path.startsWith('/company/mentor') && !route.path.startsWith('/company/mentors')) return 'mentor-home'
  if (route.path.startsWith('/company/internships/') && route.path.endsWith('/evaluation')) return 'evaluation'
  if (route.path.startsWith('/company/dashboard')) return 'dashboard'
  if (route.path.startsWith('/company/profile')) return 'profile'
  if (route.path.startsWith('/company/jobs')) return 'jobs'
  if (route.path.startsWith('/company/applications')) return 'applications'
  if (route.path.startsWith('/company/internships')) return 'internships'
  if (route.path.startsWith('/company/mentors')) return 'mentors'
  if (route.path.startsWith('/company/notifications')) return 'notifications'
  return ''
})

function toFor(item) {
  return { path: item.path, query: { role: currentRole.value } }
}
</script>

<template>
  <aside class="w-[232px] min-h-screen flex-shrink-0 text-white flex flex-col" style="background:#00203D;">
    <div class="px-5 py-5 border-b border-white/10 text-lg font-semibold">实习管理系统</div>
    <div class="mx-3 mt-3 px-4 py-3 rounded-lg border border-white/10 bg-white/5">
      <p class="text-[12px] text-white/60 mb-1">当前企业身份</p>
      <p class="text-[14px] font-semibold text-white">{{ currentRoleLabel }}</p>
    </div>
    <nav class="flex-1 py-3 px-3 space-y-1">
      <router-link
        v-for="item in visibleMenuItems"
        :key="item.key"
        :to="toFor(item)"
        class="flex items-center px-4 py-3 rounded-lg text-[16px] hover:bg-white/10 hover:text-white"
        :class="activeKey === item.key ? 'text-[#C7D2FE]' : 'text-white/80'"
      >
        {{ item.label }}
      </router-link>
    </nav>
  </aside>
</template>
