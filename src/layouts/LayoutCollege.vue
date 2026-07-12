<!--
LayoutCollege.vue — 学院管理端后台页面统一外壳
适用角色：学院管理员
-->
<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const menuItems = [
  { label: '工作台',         key: '工作台',         path: '/college/dashboard' },
  { label: '企业入驻审核',   key: '企业入驻审核',   path: '/college/companies/review' },
  { label: '学生申请复核',   key: '学生申请复核',   path: '/college/applications/review' },
  { label: '通知公告',       key: '通知公告',       path: '/college/notifications' },
  { label: '消息中心',       key: '消息中心',       path: '/college/messages' },
]

const activeKey = computed(() => {
  const exact = menuItems.find(m => m.path === route.path)
  if (exact) return exact.key
  if (route.path.startsWith('/college/companies')) return '企业入驻审核'
  if (route.path.startsWith('/college/applications')) return '学生申请复核'
  if (route.path.startsWith('/college/notifications')) return '通知公告'
  if (route.path.startsWith('/college/messages')) return '消息中心'
  return '工作台'
})

function navigate(item) { router.push(item.path).catch(() => {}) }

const userName = ref('张院长')
const userRole = ref('学院管理员')
</script>

<template>
  <div class="fixed inset-0 flex bg-[#e5e7eb]">
    <aside class="w-[232px] bg-[#00203d] shrink-0 flex flex-col select-none">
      <div class="h-[72px] flex items-center px-[24px]">
        <span class="text-white text-[18px] font-bold tracking-wide">实习管理系统</span>
      </div>
      <nav class="flex-1 pt-[18px]">
        <div v-for="item in menuItems" :key="item.key"
          class="relative flex items-center gap-[14px] cursor-pointer text-[15px] h-[44px] mx-[8px] mb-[2px] px-[18px] rounded-[8px] transition-colors hover:bg-[rgba(255,255,255,0.06)]"
          :class="activeKey === item.key
            ? 'text-white bg-[rgba(37,99,235,0.18)] border-l-[3px] border-[#2563eb] pl-[15px]'
            : 'text-[#c7d2fe] border-l-[3px] border-transparent pl-[15px]'"
          @click="navigate(item)">
          <span class="w-[10px] h-[10px] rounded-[2px] shrink-0"
            :class="activeKey === item.key ? 'bg-[#2563eb]' : 'bg-[rgba(199,210,254,0.4)]'" />
          <span>{{ item.label }}</span>
        </div>
      </nav>
    </aside>
    <div class="flex-1 flex flex-col min-w-0">
      <header class="h-[72px] bg-white flex items-center justify-between px-[24px] shrink-0 border-b border-[#e5e7eb]">
        <div class="text-[#757575] text-[14px]">首页 / {{ route.meta?.title || '' }}</div>
        <div class="flex items-center gap-[12px]">
          <div class="w-[40px] h-[40px] rounded-full bg-[#d1d5db] flex items-center justify-center text-[14px] font-medium text-[#6b7280]">张</div>
          <div><div class="text-[14px] font-medium">{{ userName }}</div><div class="text-[12px] text-[#dc2626]">{{ userRole }}</div></div>
          <span class="text-[14px] font-medium text-[#ff383c] cursor-pointer ml-[8px]">登出</span>
        </div>
      </header>
      <main class="flex-1 overflow-auto bg-[#f5f7fb]"><slot /></main>
    </div>
  </div>
</template>
