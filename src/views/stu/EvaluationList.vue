<!--
页面编号：STU-EVAL-01
页面名称：成绩查看
路由：/stu/evaluations
Figma Node：74:54
当前阶段：阶段一-原型还原
-->
<script setup>
import LayoutBackend from '../../layouts/LayoutBackend.vue'
import { evaluationData } from '../../mock/stu.js'

const { composite, items, comments, info } = evaluationData

// 根据分数返回等级颜色
function scoreColor(score) {
  if (score >= 90) return '#16A34A'
  if (score >= 80) return '#2563EB'
  if (score >= 60) return '#F59E0B'
  return '#FF383C'
}

function scoreGrade(score) {
  if (score >= 90) return '优秀'
  if (score >= 80) return '良好'
  if (score >= 60) return '合格'
  return '不合格'
}
</script>

<template>
  <LayoutBackend>
    <div class="p-[30px_40px]">

      <!-- ===== 页面标题 ===== -->
      <h1 class="text-[32px] font-bold text-black mb-[4px]">成绩查看</h1>
      <p class="text-[16px] text-[#757575] mb-[24px]">查看你的实习综合成绩与导师评价</p>

      <!-- ===== 实习基本信息 ===== -->
      <div class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] p-[24px] mb-[22px]">
        <div class="grid grid-cols-4 gap-[16px] text-[14px]">
          <div>
            <span class="text-[#757575]">实习类型：</span>
            <span class="font-medium">{{ info.type }}</span>
          </div>
          <div>
            <span class="text-[#757575]">实习企业：</span>
            <span class="font-medium">{{ info.company }}</span>
          </div>
          <div>
            <span class="text-[#757575]">实习岗位：</span>
            <span class="font-medium">{{ info.position }}</span>
          </div>
          <div>
            <span class="text-[#757575]">实习周期：</span>
            <span class="font-medium">{{ info.period }}（{{ info.totalDays }}天）</span>
          </div>
        </div>
      </div>

      <!-- ===== 综合成绩卡片 ===== -->
      <div class="grid grid-cols-2 gap-[22px] mb-[22px]">
        <!-- 总成绩大卡片 -->
        <div class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] p-[32px] flex flex-col items-center justify-center">
          <span class="text-[14px] text-[#757575] mb-[8px]">综合成绩</span>
          <span class="text-[64px] font-bold leading-none mb-[4px]" :style="{ color: scoreColor(composite.total) }">
            {{ composite.total }}
          </span>
          <span class="text-[18px] font-semibold mb-[12px]" :style="{ color: scoreColor(composite.total) }">
            {{ composite.grade }}
          </span>
          <span class="text-[13px] text-[#9ca3af]">
            学校评分 {{ composite.schoolWeight }}% + 企业评分 {{ composite.enterpriseWeight }}%
          </span>
        </div>

        <!-- 双评分对比卡片 -->
        <div class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] p-[32px]">
          <div class="flex items-center justify-around h-full">
            <!-- 学校评分 -->
            <div class="flex flex-col items-center">
              <span class="text-[14px] text-[#757575] mb-[8px]">学校评分（{{ composite.schoolWeight }}%）</span>
              <span class="text-[48px] font-bold leading-none mb-[4px]" :style="{ color: scoreColor(composite.schoolScore) }">
                {{ composite.schoolScore }}
              </span>
              <span class="text-[14px] font-medium" :style="{ color: scoreColor(composite.schoolScore) }">
                {{ scoreGrade(composite.schoolScore) }}
              </span>
            </div>
            <!-- 分割线 -->
            <div class="w-[1px] h-[80px] bg-[#e5e7eb]" />
            <!-- 企业评分 -->
            <div class="flex flex-col items-center">
              <span class="text-[14px] text-[#757575] mb-[8px]">企业评分（{{ composite.enterpriseWeight }}%）</span>
              <span class="text-[48px] font-bold leading-none mb-[4px]" :style="{ color: scoreColor(composite.enterpriseScore) }">
                {{ composite.enterpriseScore }}
              </span>
              <span class="text-[14px] font-medium" :style="{ color: scoreColor(composite.enterpriseScore) }">
                {{ scoreGrade(composite.enterpriseScore) }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- ===== 分项评分表格 ===== -->
      <div class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] overflow-hidden mb-[22px]">
        <h2 class="text-[18px] font-semibold px-[24px] pt-[24px] pb-[16px]">分项评分明细</h2>
        <!-- 表头 -->
        <div class="grid grid-cols-[1fr_100px_120px_120px_100px] gap-[16px] px-[24px] py-[12px] bg-[#f9fafb] text-[13px] font-medium text-[#6b7280] border-y border-[#e5e7eb]">
          <div>评分项</div>
          <div>权重</div>
          <div>学校评分</div>
          <div>企业评分</div>
          <div>综合</div>
        </div>
        <!-- 数据行 -->
        <div
          v-for="(item, idx) in items"
          :key="item.name"
          class="grid grid-cols-[1fr_100px_120px_120px_100px] gap-[16px] px-[24px] py-[14px] text-[14px] items-center"
          :class="{ 'border-t border-[#f3f4f6]': idx > 0 }"
        >
          <div class="font-medium">{{ item.name }}</div>
          <div class="text-[#757575]">{{ item.weight }}</div>
          <div class="font-mono font-medium" :style="{ color: scoreColor(item.schoolScore) }">{{ item.schoolScore }}</div>
          <div class="font-mono font-medium" :style="{ color: scoreColor(item.enterpriseScore) }">{{ item.enterpriseScore }}</div>
          <div class="font-mono font-semibold" :style="{ color: scoreColor(item.composite) }">{{ item.composite }}</div>
        </div>
      </div>

      <!-- ===== 导师评语 ===== -->
      <div class="grid grid-cols-2 gap-[22px]">
        <!-- 学校指导老师评语 -->
        <div class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] p-[24px]">
          <div class="flex items-center gap-[10px] mb-[12px]">
            <div class="w-[40px] h-[40px] rounded-full bg-[#eff6ff] flex items-center justify-center text-[#2563eb] text-[15px] font-bold">
              {{ comments.schoolMentor.name[0] }}
            </div>
            <div>
              <div class="text-[14px] font-semibold">{{ comments.schoolMentor.name }}</div>
              <div class="text-[12px] text-[#757575]">{{ comments.schoolMentor.role }}</div>
            </div>
            <span class="ml-auto text-[12px] text-[#9ca3af]">{{ comments.schoolMentor.date }}</span>
          </div>
          <p class="text-[14px] text-[#374151] leading-relaxed">{{ comments.schoolMentor.content }}</p>
        </div>

        <!-- 企业指导老师评语 -->
        <div class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] p-[24px]">
          <div class="flex items-center gap-[10px] mb-[12px]">
            <div class="w-[40px] h-[40px] rounded-full bg-[#f0fdf4] flex items-center justify-center text-[#16a34a] text-[15px] font-bold">
              {{ comments.enterpriseMentor.name[0] }}
            </div>
            <div>
              <div class="text-[14px] font-semibold">{{ comments.enterpriseMentor.name }}</div>
              <div class="text-[12px] text-[#757575]">{{ comments.enterpriseMentor.role }}</div>
            </div>
            <span class="ml-auto text-[12px] text-[#9ca3af]">{{ comments.enterpriseMentor.date }}</span>
          </div>
          <p class="text-[14px] text-[#374151] leading-relaxed">{{ comments.enterpriseMentor.content }}</p>
        </div>
      </div>

    </div>
  </LayoutBackend>
</template>
