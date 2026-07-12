<!--
页面编号：STU-INTRN-02
页面名称：我的实习总览
路由：/stu/internships/overview
Figma Node：164:4225
当前阶段：阶段一-原型还原
-->
<script setup>
import LayoutBackend from '../../layouts/LayoutBackend.vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 统计卡片
const statCards = [
  { label: '当前计划', value: '1个', desc: '2024届毕业实习', color: '#2563EB' },
  { label: '申请状态', value: '审核中', desc: '等待学院审核', color: '#F59E0B' },
  { label: '已交材料', value: '3/6项', desc: '仍需补充3项', color: '#16A34A' },
  { label: '距离开始', value: '12天', desc: '06月01日开始', color: '#6B7280' },
]

// 审核进度
const auditSteps = [
  { label: '提交申请', time: '2026-05-20', done: true },
  { label: '指导老师审核', time: '2026-05-22', done: true },
  { label: '学院复核', time: '审核中', done: true, current: true },
  { label: '学校备案', time: '待完成', done: false },
  { label: '实习开始', time: '2026-06-01', done: false },
]

// 材料提醒
const materialReminders = [
  { name: '个人简历', status: '已提交', done: true },
  { name: '单位接受函', status: '待上传', done: false },
  { name: '家长知情回执', status: '待上传', done: false },
  { name: '实习保险/安全承诺', status: '待上传', done: false },
  { name: '实习计划书', status: '待上传', done: false },
  { name: '三方协议', status: '已提交', done: true },
]
</script>

<template>
  <LayoutBackend>
    <div class="p-[30px_40px]">

      <!-- ===== 页面标题 ===== -->
      <h1 class="text-[32px] font-bold text-black mb-[4px]">我的实习总览</h1>
      <p class="text-[16px] text-[#757575] mb-[24px]">查看实习进度、提交材料、管理实习过程</p>

      <!-- ===== 统计卡片组 ===== -->
      <div class="grid grid-cols-4 gap-[22px] mb-[24px]">
        <div
          v-for="card in statCards"
          :key="card.label"
          class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] p-[20px_24px] flex flex-col items-center justify-center min-h-[120px]"
        >
          <span class="text-[13px] text-[#757575] mb-[6px]">{{ card.label }}</span>
          <span class="text-[28px] font-bold" :style="{ color: card.color }">{{ card.value }}</span>
          <span class="text-[13px] text-[#9ca3af] mt-[4px]">{{ card.desc }}</span>
        </div>
      </div>

      <!-- ===== 双栏区域 ===== -->
      <div class="grid grid-cols-[612px_1fr] gap-[22px] mb-[22px]">

        <!-- 左：审核进度 -->
        <div class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] p-[24px]">
          <h2 class="text-[18px] font-semibold mb-[20px]">审核进度</h2>
          <div class="relative pl-[28px]">
            <!-- 竖线 -->
            <div class="absolute left-[7px] top-[8px] bottom-[8px] w-[2px] bg-[#e5e7eb]" />
            <div
              v-for="(step, i) in auditSteps"
              :key="i"
              class="relative mb-[32px] last:mb-0"
            >
              <!-- 节点 -->
              <div
                class="absolute left-[-21px] top-[4px] w-[16px] h-[16px] rounded-full z-10"
                :class="step.done ? 'bg-[#2563eb]' : 'bg-[#d1d5db]'"
              />
              <div
                v-if="step.current"
                class="absolute left-[-25px] top-0 w-[24px] h-[24px] rounded-full border-[3px] border-[rgba(37,99,235,0.3)] z-0"
              />
              <div class="flex items-start justify-between">
                <div>
                  <div class="text-[14px] font-medium" :class="step.current ? 'text-[#2563eb]' : step.done ? 'text-[#1f2937]' : 'text-[#9ca3af]'">
                    {{ step.label }}
                  </div>
                  <div class="text-[12px] text-[#9ca3af] mt-[2px]">{{ step.time }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 右：材料提醒 -->
        <div class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] p-[24px]">
          <h2 class="text-[18px] font-semibold mb-[16px]">材料提醒</h2>
          <div class="space-y-[6px]">
            <div
              v-for="m in materialReminders"
              :key="m.name"
              class="flex items-center justify-between py-[10px] px-[12px] rounded-[8px] bg-[#f9fafb]"
            >
              <div class="flex items-center gap-[10px]">
                <span
                  class="w-[8px] h-[8px] rounded-full"
                  :class="m.done ? 'bg-[#16a34a]' : 'bg-[#f59e0b]'"
                />
                <span class="text-[14px]" :class="m.done ? 'text-[#1f2937]' : 'text-[#374151]'">{{ m.name }}</span>
              </div>
              <span
                class="text-[12px] font-medium px-[8px] py-[1px] rounded-[12px]"
                :class="m.done
                  ? 'text-[#16a34a] bg-[rgba(20,141,61,0.25)]'
                  : 'text-[#f59e0b] bg-[rgba(245,158,11,0.1)]'"
              >{{ m.status }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- ===== 快捷入口按钮组 ===== -->
      <div class="flex items-center gap-[16px]">
        <button
          class="bg-[#2563eb] text-white text-[14px] font-medium px-[24px] py-[10px] rounded-[24px] hover:bg-[#1d4ed8] transition-colors cursor-pointer"
          @click="router.push('/stu/internships')"
        >📋 查看实习列表</button>
        <button
          class="bg-white text-[#2563eb] text-[14px] font-medium px-[24px] py-[10px] rounded-[24px] border border-[#2563eb] hover:bg-[#eff6ff] transition-colors cursor-pointer"
          @click="router.push('/stu/internships/new')"
        >+ 发起新申请</button>
        <button
          class="bg-white text-[#6b7280] text-[14px] font-medium px-[24px] py-[10px] rounded-[24px] border border-[rgba(0,0,0,0.25)] hover:bg-[#f9fafb] transition-colors cursor-pointer"
          @click="router.push('/stu/reports')"
        >📝 提交周报</button>
      </div>

    </div>
  </LayoutBackend>
</template>
