<template>
  <div class="flex min-h-screen">
    <CompanySidebar />
    <div class="flex-1 flex flex-col" style="background:#F5F7FB;">
      <header class="h-[72px] bg-white flex items-center justify-between px-6 shadow-sm flex-shrink-0">
        <span class="text-[14px]" style="color:#757575;">企业管理 / 企业管理员工作台</span>
        <div class="flex items-center gap-4">
          <div class="text-right">
            <div class="text-[14px] font-medium text-gray-900">林负责人</div>
            <div class="text-[12px]" style="color:#DC2626;">企业管理员</div>
          </div>
          <router-link to="/login" class="text-[14px] font-medium" style="color:#FF383C;">登出</router-link>
        </div>
      </header>

      <main class="flex-1 overflow-auto p-6">
        <div class="mb-6">
          <p class="text-[14px]" style="color:#757575;">企业管理 / 企业管理员工作台</p>
          <h2 class="text-[32px] font-bold text-gray-900 mt-1">企业管理员工作台</h2>
          <p class="text-[16px] mt-1" style="color:#757575;">负责企业资料、人员账号、指导老师与企业级通知。</p>
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
            :to="{ path: action.path, query: { role: 'admin' } }"
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
  { label: '认证状态', value: '已认证', color: '#16A34A' },
  { label: '启用账号', value: '8 个', color: '#2563EB' },
  { label: '停用账号', value: '1 个', color: '#7C3AED' },
  { label: '待处理通知', value: '2 条', color: '#D97706' },
]

const actions = [
  { title: '企业信息管理', desc: '维护企业基础资料、联系人与资质信息。', path: '/company/profile' },
  { title: '人员账号管理', desc: '统一管理联系人、招聘负责人、企业指导老师账号。', path: '/company/admin/accounts' },
  { title: '企业指导老师管理', desc: '维护企业导师名录、指导学生与评价进度。', path: '/company/mentors' },
  { title: '企业消息中心', desc: '查看学校公告、审核提醒和系统通知。', path: '/company/notifications' },
]
</script>
