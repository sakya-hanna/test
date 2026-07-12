<template>
  <div class="flex min-h-screen">
    <CompanySidebar />
    <div class="flex-1 flex flex-col" style="background:#F5F7FB;">
      <header class="h-[72px] bg-white flex items-center justify-between px-6 shadow-sm flex-shrink-0">
        <span class="text-[14px]" style="color:#757575;">企业管理 / 企业联系人工作台</span>
        <div class="flex items-center gap-4">
          <div class="text-right">
            <div class="text-[14px] font-medium text-gray-900">李明明</div>
            <div class="text-[12px]" style="color:#16A34A;">企业联系人</div>
          </div>
          <router-link to="/login" class="text-[14px] font-medium" style="color:#FF383C;">登出</router-link>
        </div>
      </header>

      <main class="flex-1 overflow-auto p-6">
        <div class="mb-6">
          <p class="text-[14px]" style="color:#757575;">企业管理 / 企业联系人工作台</p>
          <h2 class="text-[32px] font-bold text-gray-900 mt-1">企业联系人工作台</h2>
          <p class="text-[16px] mt-1" style="color:#757575;">负责校企沟通、学生申请跟进和实习对接过程协同。</p>
        </div>

        <div class="grid grid-cols-4 gap-5 mb-6">
          <div v-for="item in stats" :key="item.label" class="bg-white rounded-2xl p-5 shadow-sm">
            <p class="text-[13px] mb-2" style="color:#6B7280;">{{ item.label }}</p>
            <p class="text-[28px] font-bold" :style="{ color: item.color }">{{ item.value }}</p>
          </div>
        </div>

        <div class="grid grid-cols-3 gap-5">
          <router-link
            v-for="action in actions"
            :key="action.title"
            :to="{ path: action.path, query: { role: 'contact' } }"
            class="bg-white rounded-2xl p-6 shadow-sm hover:shadow-md transition-shadow"
          >
            <h3 class="text-[18px] font-semibold text-gray-900 mb-2">{{ action.title }}</h3>
            <p class="text-[14px] leading-relaxed" style="color:#6B7280;">{{ action.desc }}</p>
          </router-link>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import CompanySidebar from '../../components/CompanySidebar.vue'

const stats = [
  { label: '待沟通申请', value: '6 份', color: '#D97706' },
  { label: '对接学生', value: '24 人', color: '#2563EB' },
  { label: '进行中实习', value: '18 人', color: '#16A34A' },
  { label: '未读消息', value: '5 条', color: '#7C3AED' },
]

const actions = [
  { title: '学生申请管理', desc: '跟进学生申请详情与沟通记录。', path: '/company/applications' },
  { title: '实习对接管理', desc: '查看学生实习进度、状态和企业导师评价进展。', path: '/company/internships' },
  { title: '企业消息中心', desc: '处理校方通知、学生提醒和系统消息。', path: '/company/notifications' },
]
</script>
