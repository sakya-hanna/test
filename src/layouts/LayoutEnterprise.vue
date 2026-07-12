<!--
LayoutEnterprise.vue — 企业端后台页面统一外壳
适用角色：企业
-->
<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const menuItems = [
  { label: '企业工作台',   key: '工作台',     path: '/company/dashboard' },
  { label: '企业信息管理', key: '企业信息',   path: '/company/profile' },
  { label: '实习需求管理', key: '实习需求',   path: '/company/jobs' },
  { label: '学生申请管理', key: '学生申请',   path: '/company/applications' },
  { label: '实习对接管理', key: '实习对接',   path: '/company/internships' },
  { label: '企业指导老师', key: '指导老师',   path: '/company/mentors' },
  { label: '企业消息中心', key: '消息中心',   path: '/company/notifications' },
]

const activeKey = computed(() => {
  const exact = menuItems.find(m => m.path === route.path)
  if (exact) return exact.key
  if (route.path.startsWith('/company/dashboard')) return '工作台'
  if (route.path.startsWith('/company/profile')) return '企业信息'
  if (route.path.startsWith('/company/jobs')) return '实习需求'
  if (route.path.startsWith('/company/applications')) return '学生申请'
  if (route.path.startsWith('/company/internships')) return '实习对接'
  if (route.path.startsWith('/company/mentors')) return '指导老师'
  if (route.path.startsWith('/company/notifications')) return '消息中心'
  return '工作台'
})

function navigate(item) {
  router.push(item.path).catch(() => {})
}

defineProps({
  userName: { type: String, default: '华为' },
  userRole: { type: String, default: '企业用户' },
  userAvatar: { type: String, default: '华' },
})
</script>

<template>
  <div class="fixed inset-0 flex bg-[#e5e7eb]">
    <!-- 侧边栏 -->
    <aside class="w-[232px] bg-[#00203d] shrink-0 flex flex-col select-none">
      <div class="h-[72px] flex items-center px-[24px]">
        <span class="text-white text-[18px] font-bold tracking-wide">实习管理系统</span>
      </div>
      <nav class="flex-1 pt-[18px]">
        <div
          v-for="item in menuItems"
          :key="item.key"
          class="relative flex items-center gap-[14px] cursor-pointer text-[15px] h-[44px] mx-[8px] mb-[2px] px-[18px] rounded-[8px] transition-colors hover:bg-[rgba(255,255,255,0.06)]"
          :class="activeKey === item.key
            ? 'text-white bg-[rgba(37,99,235,0.18)] border-l-[3px] border-[#2563eb] pl-[15px]'
            : 'text-[#c7d2fe] border-l-[3px] border-transparent pl-[15px]'"
          @click="navigate(item)"
        >
          <span
            class="w-[10px] h-[10px] rounded-[2px] shrink-0"
            :class="activeKey === item.key ? 'bg-[#2563eb]' : 'bg-[rgba(199,210,254,0.4)]'"
          />
          <span>{{ item.label }}</span>
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
            <div class="text-[12px] text-[#16a34a]">{{ userRole }}</div>
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
