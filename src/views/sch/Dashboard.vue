<!--
页面编号：SCH-DASH-01
页面名称：学校管理工作台
路由：/school/dashboard
Figma Node：26:176
当前阶段：阶段一-原型还原
-->
<script setup>
import LayoutSchool from '../../layouts/LayoutSchool.vue'
import { schDashboardStats } from '../../mock/sch.js'
import { useRouter } from 'vue-router'

const router = useRouter()
const stats = schDashboardStats

function go(link) {
  if (link) router.push(link)
}
</script>

<template>
  <LayoutSchool>
    <div class="p-[30px_40px]">

      <h1 class="text-[32px] font-bold text-black mb-[4px]">学校管理工作台</h1>
      <p class="text-[16px] text-[#757575] mb-[24px]">
        欢迎回来，王处长 — 你有 {{ stats.pendingItems.length }} 项待处理工作
      </p>

      <!-- 统计卡片 -->
      <div class="grid grid-cols-4 gap-[22px] mb-[25px]">
        <div
          v-for="card in stats.statCards"
          :key="card.label"
          class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] p-[20px_24px] flex flex-col justify-between min-h-[100px]"
        >
          <span class="text-[14px] text-[#757575]">{{ card.label }}</span>
          <span class="text-[36px] font-bold" :style="{ color: card.color }">{{ card.value }}</span>
        </div>
      </div>

      <!-- 双栏 -->
      <div class="grid grid-cols-2 gap-[22px] mb-[22px]">
        <!-- 待办 -->
        <div class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] p-[24px]">
          <h2 class="text-[18px] font-semibold mb-[4px]">待办事项</h2>
          <p class="text-[14px] text-[#757575] mb-[12px]">{{ stats.pendingItems.length }} 项待处理</p>
          <div
            v-for="item in stats.pendingItems"
            :key="item.id"
            class="flex items-center justify-between py-[10px] px-[12px] mb-[6px] rounded-[8px] bg-[#f9fafb] cursor-pointer hover:bg-[#f3f4f6] transition-colors"
            @click="go(item.link)"
          >
            <div class="min-w-0 flex-1">
              <div class="text-[14px] font-medium truncate">{{ item.text }}</div>
            </div>
            <span class="text-[12px] shrink-0 ml-[12px] px-[8px] py-[2px] rounded-[12px]"
              :class="item.type === '复核' ? 'text-[#f59e0b] bg-[rgba(245,158,11,0.1)]' :
                       item.type === '发布' ? 'text-[#2563eb] bg-[rgba(37,99,235,0.1)]' :
                       item.type === '通知' ? 'text-[#7c3aed] bg-[rgba(124,58,237,0.1)]' :
                       'text-[#6b7280] bg-[rgba(0,0,0,0.05)]'"
            >{{ item.type }}</span>
          </div>
        </div>

        <!-- 通知 -->
        <div class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] p-[24px]">
          <h2 class="text-[18px] font-semibold mb-[16px]">最新通知</h2>
          <div v-for="n in stats.recentNotices" :key="n.id"
            class="flex items-center justify-between py-[10px] px-[12px] mb-[6px] rounded-[8px] bg-[#f9fafb] last:mb-0"
          >
            <div class="min-w-0 flex-1">
              <div class="text-[14px] font-medium truncate">{{ n.title }}</div>
              <div class="text-[12px] text-[#9ca3af] mt-[2px]">{{ n.from }}</div>
            </div>
            <span class="text-[12px] text-[#9ca3af] shrink-0 ml-[12px]">{{ n.time }}</span>
          </div>
        </div>
      </div>

      <!-- 快捷入口 -->
      <div class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] p-[24px]">
        <h2 class="text-[18px] font-semibold mb-[24px]">快捷入口</h2>
        <div class="grid grid-cols-4 gap-[16px]">
          <div
            v-for="action in stats.quickActions"
            :key="action.label"
            class="flex flex-col items-center justify-center bg-[#f9fafb] rounded-[12px] p-[20px_8px] cursor-pointer hover:bg-[#eff6ff] hover:text-[#2563eb] transition-colors min-h-[100px]"
            @click="go(action.link)"
          >
            <span class="text-[28px] mb-[8px]">{{ action.icon }}</span>
            <span class="text-[14px] font-medium">{{ action.label }}</span>
          </div>
        </div>
      </div>

    </div>
  </LayoutSchool>
</template>
