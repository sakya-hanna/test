<!--
页面编号：STU-RPT-00
页面名称：过程记录（周报月报入口）
路由：/stu/reports
Figma：待补画
当前阶段：阶段一-原型还原
-->
<script setup>
import { computed } from 'vue'
import LayoutBackend from '../../layouts/LayoutBackend.vue'
import { useRouter } from 'vue-router'
import {
  processRecordStats,
  recentSubmissions,
  quickEntryCards,
  reportSchedule,
} from '../../mock/stu.js'

const router = useRouter()

const stats = processRecordStats
const submissions = recentSubmissions
const cards = quickEntryCards
const schedule = reportSchedule

const totalSubmitted = computed(() => stats.slice(0, 3).reduce((s, c) => s + c.value, 0))
const pendingCount = computed(() => stats.find(s => s.label === '待提交')?.value ?? 0)

function go(link) {
  if (link) router.push(link)
}

function getStatusClass(status) {
  switch (status) {
    case '已提交': return 'text-[#16a34a] bg-[rgba(20,141,61,0.25)]'
    case '已批阅': return 'text-[#2563eb] bg-[rgba(37,99,235,0.12)]'
    case '审核中': return 'text-[#f59e0b] bg-[rgba(245,158,11,0.15)]'
    default: return 'text-[#9ca3af] bg-[rgba(0,0,0,0.06)]'
  }
}
</script>

<template>
  <LayoutBackend>
    <div class="p-[30px_40px]">

      <!-- ===== 页面标题 ===== -->
      <h1 class="text-[32px] font-bold text-black mb-[4px]">过程记录</h1>
      <p class="text-[16px] text-[#757575] mb-[24px]">
        管理实习过程中的周报、月报与成果，共提交 {{ totalSubmitted }} 篇，{{ pendingCount }} 项待处理
      </p>

      <!-- ===== 统计卡片组 ===== -->
      <div class="grid grid-cols-4 gap-[22px] mb-[25px]">
        <div
          v-for="card in stats"
          :key="card.label"
          class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] p-[20px_24px] flex flex-col justify-between min-h-[100px]"
        >
          <span class="text-[14px] text-[#757575]">{{ card.label }}</span>
          <div class="flex items-baseline gap-[4px]">
            <span class="text-[36px] font-bold" :style="{ color: card.color }">{{ card.value }}</span>
            <span class="text-[14px] text-[#9ca3af]">{{ card.unit }}</span>
          </div>
        </div>
      </div>

      <!-- ===== 快捷入口卡片 ===== -->
      <div class="grid grid-cols-3 gap-[20px] mb-[25px]">
        <button
          v-for="card in cards"
          :key="card.key"
          class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] p-[28px_24px] text-left cursor-pointer hover:shadow-md hover:border-[#2563eb]/30 transition-all group"
          @click="go(card.link)"
        >
          <div class="flex items-start justify-between mb-[16px]">
            <span class="text-[36px]">{{ card.icon }}</span>
            <span
              class="text-[12px] font-medium px-[10px] py-[3px] rounded-[12px]"
              :style="{ color: card.hintColor, background: card.hintColor + '15' }"
            >{{ card.hint }}</span>
          </div>
          <div class="text-[16px] font-semibold text-black mb-[4px] group-hover:text-[#2563eb] transition-colors">
            {{ card.title }}
          </div>
          <div class="text-[13px] text-[#9ca3af]">{{ card.desc }}</div>

          <!-- 进度条 -->
          <div v-if="card.key === 'weekly'" class="mt-[16px]">
            <div class="flex justify-between text-[12px] text-[#9ca3af] mb-[6px]">
              <span>提交进度</span>
              <span>{{ schedule.weekly.submitted }}/{{ schedule.weekly.total }} 篇</span>
            </div>
            <div class="w-full h-[5px] bg-[#e5e7eb] rounded-full">
              <div
                class="h-full bg-[#2563eb] rounded-full transition-all"
                :style="{ width: (schedule.weekly.submitted / schedule.weekly.total * 100) + '%' }"
              />
            </div>
          </div>
          <div v-else-if="card.key === 'monthly'" class="mt-[16px]">
            <div class="flex justify-between text-[12px] text-[#9ca3af] mb-[6px]">
              <span>提交进度</span>
              <span>{{ schedule.monthly.submitted }}/{{ schedule.monthly.total }} 篇</span>
            </div>
            <div class="w-full h-[5px] bg-[#e5e7eb] rounded-full">
              <div
                class="h-full bg-[#16a34a] rounded-full transition-all"
                :style="{ width: (schedule.monthly.submitted / schedule.monthly.total * 100) + '%' }"
              />
            </div>
          </div>
          <div v-else class="mt-[16px]">
            <div class="flex justify-between text-[12px] text-[#9ca3af] mb-[6px]">
              <span>材料进度</span>
              <span>{{ schedule.achievement.submitted }}/{{ schedule.achievement.total }} 项</span>
            </div>
            <div class="w-full h-[5px] bg-[#e5e7eb] rounded-full">
              <div
                class="h-full bg-[#7c3aed] rounded-full transition-all"
                :style="{ width: (schedule.achievement.submitted / schedule.achievement.total * 100) + '%' }"
              />
            </div>
          </div>
        </button>
      </div>

      <!-- ===== 截止提醒 ===== -->
      <div class="flex items-center gap-[10px] flex-wrap mb-[25px]">
        <div class="flex items-center gap-[6px] bg-[#fef3c7] border border-[#fcd34d] rounded-[10px] px-[16px] py-[8px] text-[13px] text-[#92400e]">
          ⏰ 下周报截止：{{ schedule.weekly.nextDeadline }}
        </div>
        <div class="flex items-center gap-[6px] bg-[#fef3c7] border border-[#fcd34d] rounded-[10px] px-[16px] py-[8px] text-[13px] text-[#92400e]">
          📅 下月报截止：{{ schedule.monthly.nextDeadline }}
        </div>
        <div class="flex items-center gap-[6px] bg-[#fee2e2] border border-[#fca5a5] rounded-[10px] px-[16px] py-[8px] text-[13px] text-[#991b1b]">
          🚨 成果材料截止：{{ schedule.achievement.deadline }}
        </div>
      </div>

      <!-- ===== 最近提交记录 ===== -->
      <div class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)]">
        <div class="flex items-center justify-between px-[24px] pt-[22px] pb-[16px]">
          <h2 class="text-[18px] font-semibold text-black">最近提交记录</h2>
          <span class="text-[13px] text-[#9ca3af]">共 {{ submissions.length }} 条</span>
        </div>

        <div v-if="submissions.length === 0" class="px-[24px] pb-[30px] text-center text-[14px] text-[#9ca3af]">
          暂无提交记录
        </div>

        <div v-else class="divide-y divide-[#f3f4f6]">
          <div
            v-for="item in submissions"
            :key="item.id"
            class="flex items-center gap-[16px] px-[24px] py-[16px] hover:bg-[#f9fafb] cursor-pointer transition-colors"
            @click="go(item.link)"
          >
            <!-- 类型图标 -->
            <div
              class="w-[40px] h-[40px] rounded-[10px] flex items-center justify-center text-[18px] shrink-0"
              :class="{
                'bg-[#eff6ff]': item.type === '周报',
                'bg-[#f0fdf4]': item.type === '月报',
                'bg-[#f5f3ff]': item.type === '成果',
              }"
            >
              {{ item.type === '周报' ? '📝' : item.type === '月报' ? '📊' : '📄' }}
            </div>

            <!-- 内容 -->
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-[8px] mb-[2px]">
                <span class="text-[15px] font-medium text-black">{{ item.title }}</span>
                <span
                  class="text-[11px] font-medium px-[8px] py-[1px] rounded-[10px] shrink-0"
                  :class="getStatusClass(item.status)"
                >{{ item.status }}</span>
              </div>
              <p class="text-[13px] text-[#6b7280] truncate">{{ item.summary }}</p>
            </div>

            <!-- 日期 -->
            <span class="text-[13px] text-[#9ca3af] shrink-0">{{ item.date }}</span>

            <!-- 箭头 -->
            <span class="text-[#d1d5db] text-[14px] shrink-0">›</span>
          </div>
        </div>
      </div>

    </div>
  </LayoutBackend>
</template>
