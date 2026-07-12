<!--
页面编号：SCH-DASH-02
页面名称：统计分析看板
路由：/school/analytics
Figma Node：116:15
当前阶段：阶段一-原型还原
-->
<script setup>
import { computed } from 'vue'
import LayoutSchool from '../../layouts/LayoutSchool.vue'
import { analyticsData } from '../../mock/sch.js'

function exportReport() { alert('导出报表（演示） — 原型展示，待后端接入') }

const { overview, byType, byCollege, monthlyTrend, statusDist } = analyticsData

// 月度趋势最大值用于比例
const maxMonthly = Math.max(...monthlyTrend.map(m => m.count))
// 学院最大值
const maxCollege = Math.max(...byCollege.map(c => c.count))
</script>

<template>
  <LayoutSchool>
    <div class="p-[30px_40px]">

      <div class="flex items-start justify-between mb-[24px]">
        <div>
          <h1 class="text-[32px] font-bold text-black mb-[4px]">统计分析看板</h1>
          <p class="text-[16px] text-[#757575]">全校实习数据总览与分析</p>
        </div>
        <button @click="exportReport" class="bg-white border border-[#2563eb] text-[#2563eb] text-[14px] font-medium px-[20px] py-[8px] rounded-[24px] hover:bg-[#eff6ff] cursor-pointer transition-colors">
          📥 导出报表
        </button>
      </div>

      <!-- 概览卡片 -->
      <div class="grid grid-cols-4 gap-[22px] mb-[22px]">
        <div v-for="card in overview" :key="card.label"
          class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] p-[20px_24px] flex flex-col justify-between min-h-[100px]">
          <span class="text-[14px] text-[#757575]">{{ card.label }}</span>
          <div class="flex items-baseline gap-[8px]">
            <span class="text-[36px] font-bold text-black">{{ card.value }}</span>
            <span class="text-[13px]" :class="card.up ? 'text-[#16a34a]' : 'text-[#ff383c]'">{{ card.change }}</span>
          </div>
        </div>
      </div>

      <!-- 双栏 -->
      <div class="grid grid-cols-2 gap-[22px] mb-[22px]">

        <!-- 按类型分布 -->
        <div class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] p-[24px]">
          <h2 class="text-[16px] font-semibold mb-[16px]">实习类型分布</h2>
          <div v-for="item in byType" :key="item.name" class="mb-[14px] last:mb-0">
            <div class="flex justify-between text-[13px] mb-[4px]">
              <span class="text-[#374151]">{{ item.name }}</span>
              <span class="font-medium">{{ item.count }}人 ({{ item.percent }}%)</span>
            </div>
            <div class="w-full h-[8px] bg-[#f3f4f6] rounded-full overflow-hidden">
              <div class="h-full rounded-full transition-all"
                :style="{ width: item.percent + '%', background: item.name === '毕业实习' ? '#2563EB' : item.name === '灵活实习' ? '#7C3AED' : item.name === '认知实习' ? '#F59E0B' : '#16A34A' }" />
            </div>
          </div>
        </div>

        <!-- 按学院分布 -->
        <div class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] p-[24px]">
          <h2 class="text-[16px] font-semibold mb-[16px]">各学院实习生人数</h2>
          <div v-for="col in byCollege" :key="col.name" class="flex items-center gap-[10px] mb-[10px] last:mb-0">
            <span class="text-[13px] text-[#374151] w-[120px] shrink-0 truncate">{{ col.name }}</span>
            <div class="flex-1 h-[8px] bg-[#f3f4f6] rounded-full overflow-hidden">
              <div class="h-full bg-[#2563eb] rounded-full" :style="{ width: (col.count / maxCollege * 100) + '%' }" />
            </div>
            <span class="text-[13px] font-medium w-[40px] text-right">{{ col.count }}</span>
          </div>
        </div>
      </div>

      <!-- 双栏下排 -->
      <div class="grid grid-cols-2 gap-[22px]">

        <!-- 月度趋势 -->
        <div class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] p-[24px]">
          <h2 class="text-[16px] font-semibold mb-[16px]">月度新增趋势</h2>
          <div class="flex items-end justify-between gap-[8px]" style="height: 140px;">
            <div v-for="m in monthlyTrend" :key="m.month" class="flex-1 flex flex-col items-center gap-[6px]">
              <span class="text-[12px] font-medium text-[#374151]">{{ m.count }}</span>
              <div class="w-full bg-[#2563eb] rounded-t-[4px] transition-all"
                :style="{ height: (m.count / maxMonthly * 110) + 'px', opacity: 0.6 + (m.count / maxMonthly * 0.4) }" />
              <span class="text-[11px] text-[#9ca3af]">{{ m.month }}</span>
            </div>
          </div>
        </div>

        <!-- 状态分布 -->
        <div class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] p-[24px]">
          <h2 class="text-[16px] font-semibold mb-[16px]">实习状态分布</h2>
          <div class="grid grid-cols-2 gap-[12px]">
            <div v-for="s in statusDist" :key="s.name"
              class="bg-[#f9fafb] rounded-[12px] p-[16px] text-center">
              <div class="text-[28px] font-bold mb-[4px]" :style="{ color: s.color }">{{ s.count }}</div>
              <div class="text-[13px] text-[#757575]">{{ s.name }}</div>
            </div>
          </div>
          <!-- 环形占比示意 -->
          <div class="mt-[16px] flex justify-center gap-[16px]">
            <span v-for="s in statusDist" :key="s.name" class="flex items-center gap-[4px] text-[12px] text-[#757575]">
              <span class="w-[8px] h-[8px] rounded-full inline-block" :style="{ background: s.color }" />
              {{ s.name }} {{ Math.round(s.count / statusDist.reduce((a,b) => a+b.count, 0) * 100) }}%
            </span>
          </div>
        </div>
      </div>

    </div>
  </LayoutSchool>
</template>
