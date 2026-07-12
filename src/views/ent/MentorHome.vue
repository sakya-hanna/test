<template>
  <div class="flex min-h-screen">
    <CompanySidebar />
    <div class="flex-1 flex flex-col" style="background:#F5F7FB;">
      <header class="h-[72px] bg-white flex items-center justify-between px-6 shadow-sm flex-shrink-0">
        <span class="text-[14px]" style="color:#757575;">企业管理 / 企业导师工作台</span>
        <div class="flex items-center gap-4">
          <div class="text-right">
            <div class="text-[14px] font-medium text-gray-900">陈志远</div>
            <div class="text-[12px]" style="color:#16A34A;">企业指导老师</div>
          </div>
          <router-link to="/login" class="text-[14px] font-medium" style="color:#FF383C;">登出</router-link>
        </div>
      </header>

      <main class="flex-1 overflow-auto p-6">
        <div class="mb-6">
          <p class="text-[14px]" style="color:#757575;">企业管理 / 企业导师工作台</p>
          <h2 class="text-[32px] font-bold text-gray-900 mt-1">企业导师工作台</h2>
          <p class="text-[16px] mt-1" style="color:#757575;">负责被分配学生的过程跟踪、实习评价和沟通提醒。</p>
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
            :to="{ path: action.path, query: { role: 'mentor' } }"
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
  { label: '指导学生', value: '6 人', color: '#2563EB' },
  { label: '进行中', value: '4 人', color: '#16A34A' },
  { label: '待评价', value: '2 人', color: '#D97706' },
  { label: '未读提醒', value: '3 条', color: '#7C3AED' },
]

const actions = [
  { title: '实习对接管理', desc: '查看被分配学生的实习周期、状态和进度。', path: '/company/internships' },
  { title: '学生评价', desc: '填写学生在岗表现评价和评分。', path: '/company/internships/1/evaluation' },
  { title: '企业消息中心', desc: '查看评价提醒、学生消息和学校公告。', path: '/company/notifications' },
]
</script>
