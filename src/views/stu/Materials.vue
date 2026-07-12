<!--
页面编号：STU-RPT-03-MAT
页面名称：材料提交入口
路由：/stu/materials
Figma：待补画
当前阶段：阶段一-原型还原
-->
<script setup>
import { computed, ref } from 'vue'
import LayoutBackend from '../../layouts/LayoutBackend.vue'
import { useRouter } from 'vue-router'
import {
  materialStats,
  materialChecklist,
  materialSubmissionProgress,
} from '../../mock/stu.js'

const router = useRouter()

const stats = materialStats
const materials = ref(materialChecklist.map(m => ({ ...m })))
const progress = materialSubmissionProgress

const submittedCount = computed(() => materials.value.filter(m => m.status === '已上传').length)
const pendingCount = computed(() => materials.value.filter(m => m.status === '待上传').length)
const requiredDone = computed(() => materials.value.filter(m => m.required && m.status !== '待上传').length)
const requiredTotal = computed(() => materials.value.filter(m => m.required).length)
const progressPercent = computed(() => Math.round((requiredDone.value / requiredTotal.value) * 100))

function getStatusTag(item) {
  switch (item.status) {
    case '已上传': return { text: '已上传', class: 'text-[#16a34a] bg-[rgba(20,141,61,0.25)]' }
    case '待上传': return { text: '待上传', class: 'text-[#9ca3af] bg-[rgba(0,0,0,0.06)]' }
    case '审核中': return { text: '审核中', class: 'text-[#f59e0b] bg-[rgba(245,158,11,0.15)]' }
    default: return { text: item.status, class: 'text-[#9ca3af] bg-[rgba(0,0,0,0.06)]' }
  }
}

function uploadMaterial(item) {
  if (item.status === '已上传' || item.status === '审核中') return
  item.status = '已上传'
  item.file = `${item.name}_文件.pdf`
  item.fileSize = '1.5 MB'
  item.uploadedAt = new Date().toLocaleString('zh-CN')
}

function replaceMaterial(item) {
  // TODO: 阶段二真实替换
  alert(`重新上传 ${item.name}`)
}

function goSubmitAll() {
  router.push('/stu/achievements/new')
}
</script>

<template>
  <LayoutBackend>
    <div class="p-[30px_40px]">

      <!-- ===== 页面标题 ===== -->
      <h1 class="text-[32px] font-bold text-black mb-[4px]">材料提交</h1>
      <p class="text-[16px] text-[#757575] mb-[24px]">
        提交实习所需材料，必填项 {{ requiredDone }}/{{ requiredTotal }} 已完成，共 {{ submittedCount }} 项已上传
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

      <!-- ===== 双栏布局：进度 + 提醒 ===== -->
      <div class="grid grid-cols-[1fr_340px] gap-[22px] mb-[22px]">

        <!-- 左：总体进度卡片 -->
        <div class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] p-[24px]">
          <h2 class="text-[18px] font-semibold text-black mb-[4px]">提交进度总览</h2>
          <p class="text-[13px] text-[#9ca3af] mb-[20px]">必填材料完成情况</p>

          <!-- 大进度环 -->
          <div class="flex items-center gap-[28px]">
            <div class="relative w-[100px] h-[100px] shrink-0">
              <svg class="w-full h-full -rotate-90" viewBox="0 0 100 100">
                <circle cx="50" cy="50" r="42" fill="none" stroke="#e5e7eb" stroke-width="8" />
                <circle
                  cx="50" cy="50" r="42" fill="none"
                  :stroke="progressPercent >= 100 ? '#16a34a' : '#2563eb'"
                  stroke-width="8" stroke-linecap="round"
                  :stroke-dasharray="2 * Math.PI * 42"
                  :stroke-dashoffset="2 * Math.PI * 42 * (1 - progressPercent / 100)"
                  class="transition-all duration-700"
                />
              </svg>
              <div class="absolute inset-0 flex items-center justify-center">
                <span class="text-[22px] font-bold" :style="{ color: progressPercent >= 100 ? '#16a34a' : '#2563eb' }">
                  {{ progressPercent }}%
                </span>
              </div>
            </div>

            <div class="flex-1 space-y-[14px]">
              <div class="flex items-center justify-between text-[14px]">
                <span class="text-[#6b7280]">必填材料</span>
                <span class="font-medium">{{ requiredDone }}/{{ requiredTotal }} 项已完成</span>
              </div>
              <!-- 分项进度条 -->
              <div class="space-y-[10px]">
                <div v-for="m in materials.filter(m => m.required)" :key="m.id" class="flex items-center gap-[10px]">
                  <span
                    class="w-[8px] h-[8px] rounded-full shrink-0"
                    :class="{
                      'bg-[#16a34a]': m.status === '已上传',
                      'bg-[#f59e0b]': m.status === '审核中',
                      'bg-[#d1d5db]': m.status === '待上传',
                    }"
                  />
                  <span class="text-[13px] text-[#374151] flex-1 truncate">{{ m.name }}</span>
                  <span
                    class="text-[12px] font-medium shrink-0"
                    :class="{
                      'text-[#16a34a]': m.status === '已上传',
                      'text-[#f59e0b]': m.status === '审核中',
                      'text-[#9ca3af]': m.status === '待上传',
                    }"
                  >{{ m.status }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 右：截止提醒 + 快捷操作 -->
        <div class="space-y-[16px]">
          <!-- 截止提醒 -->
          <div class="bg-[#fef3c7] border border-[#fcd34d] rounded-[15px] p-[20px_24px]">
            <div class="flex items-center gap-[8px] mb-[8px]">
              <span class="text-[22px]">⏰</span>
              <span class="text-[15px] font-semibold text-[#92400e]">提交截止提醒</span>
            </div>
            <p class="text-[13px] text-[#92400e] mb-[12px]">
              所有材料请在 <strong>{{ progress.deadline }}</strong> 前提交，距截止还有 <strong>{{ progress.remainingDays }} 天</strong>
            </p>
            <div class="w-full h-[6px] bg-[#fde68a] rounded-full mb-[6px]">
              <div
                class="h-full bg-[#f59e0b] rounded-full"
                :style="{ width: Math.min(100, (progress.submitted / progress.total * 100)) + '%' }"
              />
            </div>
            <span class="text-[12px] text-[#a16207]">总体 {{ progress.submitted }}/{{ progress.total }} 项材料已提交</span>
          </div>

          <!-- 快捷入口 -->
          <button
            class="w-full bg-[#2563eb] text-white text-[15px] font-medium px-[24px] py-[12px] rounded-[24px] hover:bg-[#1d4ed8] transition-colors cursor-pointer text-center"
            @click="goSubmitAll"
          >
            前往成果提交页
          </button>
          <p class="text-[12px] text-[#9ca3af] text-center mt-[4px]">
            或在下表中逐项上传材料
          </p>
        </div>
      </div>

      <!-- ===== 材料清单表格 ===== -->
      <div class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)]">
        <div class="flex items-center justify-between px-[24px] pt-[22px] pb-[16px]">
          <h2 class="text-[18px] font-semibold text-black">材料清单</h2>
          <div class="flex items-center gap-[12px] text-[12px]">
            <span class="flex items-center gap-[4px] text-[#16a34a]"><span class="w-[8px] h-[8px] rounded-full bg-[#16a34a] inline-block" /> 已上传</span>
            <span class="flex items-center gap-[4px] text-[#f59e0b]"><span class="w-[8px] h-[8px] rounded-full bg-[#f59e0b] inline-block" /> 审核中</span>
            <span class="flex items-center gap-[4px] text-[#9ca3af]"><span class="w-[8px] h-[8px] rounded-full bg-[#d1d5db] inline-block" /> 待上传</span>
          </div>
        </div>

        <div class="divide-y divide-[#f3f4f6]">
          <div
            v-for="m in materials"
            :key="m.id"
            class="flex items-center gap-[20px] px-[24px] py-[18px]"
          >
            <!-- 图标 -->
            <div
              class="w-[44px] h-[44px] rounded-[10px] flex items-center justify-center text-[20px] shrink-0"
              :class="{
                'bg-[#f0fdf4]': m.status === '已上传',
                'bg-[#fef3c7]': m.status === '审核中',
                'bg-[#f3f4f6]': m.status === '待上传',
              }"
            >📄</div>

            <!-- 信息 -->
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-[10px] mb-[4px]">
                <span class="text-[15px] font-medium">{{ m.name }}</span>
                <span
                  v-if="m.required"
                  class="text-[11px] text-[#ff383c] bg-[rgba(255,56,60,0.1)] px-[6px] py-[1px] rounded-[8px] shrink-0"
                >必填</span>
                <span
                  v-else
                  class="text-[11px] text-[#9ca3af] bg-[rgba(0,0,0,0.05)] px-[6px] py-[1px] rounded-[8px] shrink-0"
                >选填</span>
                <span
                  class="text-[11px] font-medium px-[6px] py-[1px] rounded-[8px] shrink-0"
                  :class="getStatusTag(m).class"
                >{{ getStatusTag(m).text }}</span>
              </div>
              <p class="text-[13px] text-[#9ca3af] mb-[6px]">{{ m.desc }}</p>
              <div class="flex items-center gap-[16px] text-[12px] text-[#9ca3af]">
                <span v-if="m.file" class="inline-flex items-center gap-[4px] bg-[#f3f4f6] rounded-[6px] px-[8px] py-[2px] text-[#374151]">
                  📎 {{ m.file }} ({{ m.fileSize }})
                </span>
                <span v-if="m.deadline">截止：{{ m.deadline }}</span>
                <span v-if="m.uploadedAt">上传于：{{ m.uploadedAt }}</span>
              </div>
            </div>

            <!-- 操作按钮 -->
            <button
              v-if="m.status === '待上传'"
              class="bg-[#2563eb] text-white text-[14px] font-medium px-[20px] py-[8px] rounded-[6px] hover:bg-[#1d4ed8] transition-colors cursor-pointer shrink-0"
              @click="uploadMaterial(m)"
            >上传</button>
            <button
              v-else
              class="bg-white text-[#2563eb] text-[14px] font-medium px-[20px] py-[8px] rounded-[6px] border border-[#2563eb] hover:bg-[#eff6ff] transition-colors cursor-pointer shrink-0"
              @click="replaceMaterial(m)"
            >更换</button>
          </div>
        </div>
      </div>

    </div>
  </LayoutBackend>
</template>
