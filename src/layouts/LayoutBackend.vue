<!--
LayoutBackend.vue — 后台页面统一外壳（学生端侧边栏 1:1 复刻 Figma）
适用角色：学生端
阶段一用 div + Tailwind 模拟，阶段二替换为 el-menu
-->
<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

// 学生端侧边栏 — 1:1 复刻 Figma（菜单项、顺序、文案完全一致）
const menuItems = [
  { label: '学生工作台', key: '工作台', path: '/stu/dashboard' },
  { label: '实习申请',   key: '实习申请', path: '/stu/internships/new' },
  { label: '我的实习',   key: '我的实习', path: '/stu/internships' },
  { label: '过程记录',   key: '过程记录', path: '/stu/reports' },
  { label: '材料提交',   key: '材料提交', path: '/stu/materials' },
  { label: '成绩查询',   key: '成绩查询', path: '/stu/evaluations' },
  { label: '家长知情',   key: '家长知情', path: '/stu/parent-notice' },
  { label: '通知公告',   key: '通知公告', path: '/stu/messages' },
]

// 当前激活菜单（优先精确匹配路由 path，其次匹配路由名包含的菜单 key）
const activeKey = computed(() => {
  const exact = menuItems.find(m => m.path === route.path)
  if (exact) return exact.key
  // 详情页/子页面回退匹配：/stu/internships/:id → 我的实习
  if (route.path.startsWith('/stu/internships')) return '我的实习'
  if (route.path.startsWith('/stu/reports')) return '过程记录'
  if (route.path.startsWith('/stu/materials')) return '材料提交'
  if (route.path.startsWith('/stu/evaluations')) return '成绩查询'
  return '工作台'
})

function navigate(item) {
  router.push(item.path).catch(() => { /* 路由未注册时静默忽略 */ })
}

const userName = ref('张同学')
const userRole = ref('学生')
</script>

<template>
  <div class="fixed inset-0 flex bg-[#e5e7eb]">
    <!-- ===== 侧边栏（1:1 复刻 Figma） ===== -->
    <aside class="w-[232px] bg-[#00203d] shrink-0 flex flex-col select-none">

      <!-- LOGO / 品牌区 -->
      <div class="h-[72px] flex items-center px-[24px]">
        <span class="text-white text-[18px] font-bold tracking-wide">实习管理系统</span>
      </div>

      <!-- 菜单列表 -->
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
          <!-- 菜单图标（方形标记） -->
          <span
            class="w-[10px] h-[10px] rounded-[2px] shrink-0"
            :class="activeKey === item.key ? 'bg-[#2563eb]' : 'bg-[rgba(199,210,254,0.4)]'"
          />
          <span>{{ item.label }}</span>
        </div>
      </nav>
    </aside>

    <!-- ===== 右侧区域 ===== -->
    <div class="flex-1 flex flex-col min-w-0">
      <!-- 顶栏 -->
      <header class="h-[72px] bg-white flex items-center justify-between px-[24px] shrink-0 border-b border-[#e5e7eb]">
        <!-- 面包屑 -->
        <div class="text-[#757575] text-[14px]">首页 / {{ route.meta?.title || '' }}</div>

        <!-- 用户信息 -->
        <div class="flex items-center gap-[12px]">
          <div class="w-[40px] h-[40px] rounded-full bg-[#d1d5db] flex items-center justify-center text-[14px] font-medium text-[#6b7280]">张</div>
          <div>
            <div class="text-[14px] font-medium">{{ userName }}</div>
            <div class="text-[12px] text-[#dc2626]">{{ userRole }}</div>
          </div>
          <span class="text-[14px] font-medium text-[#ff383c] cursor-pointer ml-[8px]">登出</span>
        </div>
      </header>

      <!-- 主内容 -->
      <main class="flex-1 overflow-auto bg-[#f5f7fb]">
        <slot />
      </main>
    </div>
  </div>
</template>
