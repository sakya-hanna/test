<!--
LayoutCounselor.vue — 辅导员端后台页面统一外壳
适用角色：辅导员
侧边栏与 cou/Dashboard.vue 完全一致（Figma COU-DASH-01）
-->
<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const menuItems = [
  { label: '指导工作台', key: '指导工作台', path: '/counselor/dashboard' },
  { label: '我的学生',   key: '我的学生',   path: '/counselor/student-list' },
  { label: '过程反馈',   key: '过程反馈',   path: '/counselor/process-feedback' },
  { label: '学生评价',   key: '学生评价',   path: '/counselor/evaluations' },
  { label: '通知公告',   key: '通知公告',   path: '/counselor/notifications' },
]

const activeKey = computed(() => {
  const exact = menuItems.find(m => m.path === route.path)
  if (exact) return exact.key
  if (route.path.startsWith('/counselor/dashboard')) return '指导工作台'
  if (route.path.startsWith('/counselor/student-list')) return '我的学生'
  if (route.path.startsWith('/counselor/students')) return '我的学生'
  if (route.path.startsWith('/counselor/process-feedback')) return '过程反馈'
  if (route.path.startsWith('/counselor/evaluations')) return '学生评价'
  if (route.path.startsWith('/counselor/notifications')) return '通知公告'
  return '指导工作台'
})

function navigate(item) {
  if (item.path === '#') return
  router.push(item.path).catch(() => {})
}

defineProps({
  userName: { type: String, default: '孙辅导员' },
  userRole: { type: String, default: '辅导员' },
  userAvatar: { type: String, default: '孙' },
})
</script>

<template>
  <div class="fixed inset-0 flex bg-[#e5e7eb]">
    <!-- 侧边栏 — 与 cou/Dashboard.vue 完全一致 -->
    <aside class="w-[232px] bg-[#00203d] shrink-0 flex flex-col select-none">
      <div class="px-5 py-5 border-b border-white/10 text-lg font-semibold text-white">实习管理系统</div>
      <nav class="flex-1 py-3 px-3 space-y-1">
        <div
          v-for="item in menuItems"
          :key="item.key"
          class="flex items-center px-4 py-3 rounded-lg text-[16px] cursor-pointer transition-colors"
          :class="activeKey === item.key ? '' : 'text-white/80 hover:bg-white/10'"
          :style="activeKey === item.key ? { color: '#C7D2FE' } : {}"
          @click="navigate(item)"
        >
          {{ item.label }}
        </div>
      </nav>
    </aside>

    <!-- 右侧区域 -->
    <div class="flex-1 flex flex-col min-w-0">
      <header class="h-[72px] bg-white flex items-center justify-between px-[24px] shrink-0 border-b border-[#e5e7eb]">
        <div class="text-[#757575] text-[14px]">首页 / {{ route.meta?.title || '' }}</div>
        <div class="flex items-center gap-[12px]">
          <div class="w-[40px] h-[40px] rounded-full bg-[#d1d5db] flex items-center justify-center text-[14px] font-medium text-[#6b7280]">{{ userAvatar }}</div>
          <div>
            <div class="text-[14px] font-medium">{{ userName }}</div>
            <div class="text-[12px] text-[#2563eb]">{{ userRole }}</div>
          </div>
          <router-link to="/login" class="text-[14px] font-medium text-[#ff383c] cursor-pointer ml-[8px]">登出</router-link>
        </div>
      </header>
      <main class="flex-1 overflow-auto bg-[#f5f7fb]">
        <slot />
      </main>
    </div>
  </div>
</template>
