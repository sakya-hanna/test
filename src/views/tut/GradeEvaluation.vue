<!--
页面编号：TUT-EVAL-01
页面名称：成绩评定
路由：/teacher/grades
Figma Node：101:15
当前阶段：阶段一-原型还原
-->
<script setup>
import { ref, computed } from 'vue'
import LayoutTeacher from '../../layouts/LayoutTeacher.vue'
import { gradeEvaluationList, gradeStatusMap, evaluationDimensions } from '../../mock/tut.js'

// 筛选
const activeTab = ref('')
const searchQuery = ref('')

const filteredList = computed(() => {
  let list = gradeEvaluationList
  if (activeTab.value) {
    list = list.filter(item => item.status === activeTab.value)
  }
  if (searchQuery.value.trim()) {
    const q = searchQuery.value.trim().toLowerCase()
    list = list.filter(item =>
      item.studentName.toLowerCase().includes(q) ||
      item.company.toLowerCase().includes(q)
    )
  }
  return list
})

// 评定弹窗
const dialogVisible = ref(false)
const currentItem = ref(null)
const evalForm = ref({})

function openEvaluate(item) {
  currentItem.value = item
  evalForm.value = {}
  evaluationDimensions.forEach(d => {
    evalForm.value[d.name] = ''
  })
  evalForm.value.comment = ''
  dialogVisible.value = true
}

function closeDialog() {
  dialogVisible.value = false
  currentItem.value = null
}

function submitEvaluation() {
  const allFilled = evaluationDimensions.every(d => evalForm.value[d.name] !== '')
  if (!allFilled || !evalForm.value.comment.trim()) return
  // 计算加权总分
  let total = 0
  evaluationDimensions.forEach(d => {
    total += (Number(evalForm.value[d.name]) || 0) * d.weight / 100
  })
  currentItem.value.schoolScore = Math.round(total)
  currentItem.value.status = '已评定'
  closeDialog()
}

const stats = computed(() => [
  { label: '全部学生', value: gradeEvaluationList.length, color: '#2563EB' },
  { label: '待评定', value: gradeEvaluationList.filter(i => i.status === '待评定').length, color: '#F59E0B' },
  { label: '已评定', value: gradeEvaluationList.filter(i => i.status === '已评定').length, color: '#16A34A' },
  { label: '平均分', value: '84', color: '#7C3AED' },
])

const statusTabs = [
  { label: '全部', value: '' },
  { label: '待评定', value: '待评定' },
  { label: '已评定', value: '已评定' },
]
</script>

<template>
  <LayoutTeacher>
    <div class="p-[30px_40px]">

      <h1 class="text-[32px] font-bold text-black mb-[4px]">成绩评定</h1>
      <p class="text-[16px] text-[#757575] mb-[24px]">对学生实习表现进行分项评分，系统自动计算加权总分</p>

      <!-- 统计卡片 -->
      <div class="grid grid-cols-4 gap-[22px] mb-[22px]">
        <div
          v-for="card in stats"
          :key="card.label"
          class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] p-[20px_24px] flex flex-col items-center justify-center min-h-[110px]"
        >
          <span class="text-[14px] text-[#757575] mb-[4px]">{{ card.label }}</span>
          <span class="text-[32px] font-bold" :style="{ color: card.color }">{{ card.value }}</span>
        </div>
      </div>

      <!-- 筛选 + 搜索 -->
      <div class="flex items-center justify-between mb-[16px]">
        <div class="flex items-center gap-[42px]">
          <button
            v-for="tab in statusTabs"
            :key="tab.value"
            class="text-[14px] pb-[8px] border-b-[3px] transition-colors cursor-pointer"
            :class="activeTab === tab.value
              ? 'text-[#2563eb] border-[#2563eb] font-medium'
              : 'text-[#757575] border-transparent hover:text-[#374151]'"
            @click="activeTab = tab.value"
          >{{ tab.label }}</button>
        </div>
        <input
          v-model="searchQuery"
          type="text"
          placeholder="🔍 搜索学生、企业..."
          class="w-[280px] h-[36px] bg-white border border-[rgba(0,0,0,0.16)] rounded-[8px] px-[12px] text-[14px] outline-none focus:border-[#2563eb] placeholder-[#9ca3af]"
        />
      </div>

      <div class="border-t border-[#e5e7eb] mb-0" />

      <!-- 数据表格 -->
      <div class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] overflow-hidden">
        <div class="grid grid-cols-[100px_1fr_100px_90px_90px_90px] gap-[12px] px-[20px] py-[14px] bg-[#f9fafb] text-[13px] font-medium text-[#6b7280] border-b border-[#e5e7eb]">
          <div>学生</div>
          <div>企业 / 岗位</div>
          <div>结束时间</div>
          <div>企业评分</div>
          <div>学校评分</div>
          <div>操作</div>
        </div>

        <div v-if="filteredList.length === 0" class="text-center py-[60px] text-[#9ca3af] text-[14px]">
          暂无匹配的学生记录
        </div>

        <div
          v-for="(item, idx) in filteredList"
          :key="item.id"
          class="grid grid-cols-[100px_1fr_100px_90px_90px_90px] gap-[12px] px-[20px] py-[16px] text-[14px] items-center hover:bg-[#f9fafb] transition-colors"
          :class="{ 'border-t border-[#f3f4f6]': idx > 0 }"
        >
          <div class="font-medium">{{ item.studentName }}</div>
          <div>
            <div class="font-medium">{{ item.company }}</div>
            <div class="text-[12px] text-[#757575]">{{ item.position }}</div>
          </div>
          <div class="text-[#757575]">{{ item.endDate }}</div>
          <div class="font-mono font-medium" :class="item.enterpriseScore !== '-' ? 'text-[#16a34a]' : 'text-[#9ca3af]'">
            {{ item.enterpriseScore !== '-' ? item.enterpriseScore + '分' : '-' }}
          </div>
          <div class="font-mono font-medium" :class="item.schoolScore !== '-' ? 'text-[#2563eb]' : 'text-[#9ca3af]'">
            {{ item.schoolScore !== '-' ? item.schoolScore + '分' : '待评定' }}
          </div>
          <div>
            <button
              v-if="item.status === '待评定'"
              class="text-[#2563eb] text-[13px] font-medium hover:underline cursor-pointer"
              @click="openEvaluate(item)"
            >评定</button>
            <span
              v-else
              class="inline-block px-[10px] py-[2px] rounded-[12px] text-[12px] font-medium"
              :style="{ color: gradeStatusMap[item.status]?.color, background: gradeStatusMap[item.status]?.bg }"
            >已评定</span>
          </div>
        </div>
      </div>

      <!-- 评定弹窗 -->
      <Teleport to="body">
        <div v-if="dialogVisible" class="fixed inset-0 z-50 flex items-center justify-center">
          <div class="absolute inset-0 bg-black/40" @click="closeDialog" />
          <div class="relative bg-white rounded-[15px] w-[560px] max-h-[85vh] overflow-auto shadow-xl p-[32px]">
            <h2 class="text-[20px] font-bold mb-[20px]">成绩评定</h2>

            <div v-if="currentItem" class="bg-[#f9fafb] rounded-[8px] p-[16px] mb-[20px] text-[14px] space-y-[4px]">
              <div><span class="text-[#757575]">学生：</span>{{ currentItem.studentName }}（{{ currentItem.studentNo }}）</div>
              <div><span class="text-[#757575]">企业：</span>{{ currentItem.company }} · {{ currentItem.position }}</div>
              <div><span class="text-[#757575]">企业评分：</span><span class="text-[#16a34a] font-medium">{{ currentItem.enterpriseScore }}分</span></div>
            </div>

            <!-- 分项评分 -->
            <div class="mb-[20px]">
              <span class="text-[14px] font-medium mb-[12px] block">分项评分（百分制）</span>
              <div
                v-for="dim in evaluationDimensions"
                :key="dim.name"
                class="flex items-center gap-[12px] mb-[10px]"
              >
                <span class="text-[13px] text-[#374151] w-[160px] shrink-0">{{ dim.name }}</span>
                <span class="text-[12px] text-[#9ca3af] w-[40px] shrink-0">权重{{ dim.weight }}%</span>
                <input
                  v-model="evalForm[dim.name]"
                  type="number"
                  min="0"
                  max="100"
                  placeholder="0-100"
                  class="w-[80px] h-[36px] bg-white border border-[rgba(0,0,0,0.16)] rounded-[8px] px-[10px] text-[14px] outline-none focus:border-[#2563eb] placeholder-[#9ca3af] text-center"
                />
                <span class="text-[12px] text-[#9ca3af]">/ 100</span>
              </div>
            </div>

            <!-- 评语 -->
            <div class="mb-[24px]">
              <span class="text-[14px] font-medium mb-[8px] block">综合评语 <span class="text-[#ff383c]">*</span></span>
              <textarea
                v-model="evalForm.comment"
                placeholder="请输入综合评语..."
                class="w-full h-[100px] bg-white border border-[rgba(0,0,0,0.16)] rounded-[8px] p-[12px] text-[14px] outline-none focus:border-[#2563eb] resize-none placeholder-[#9ca3af]"
              />
            </div>

            <div class="flex justify-end gap-[12px]">
              <button
                class="px-[24px] py-[10px] rounded-[24px] text-[14px] font-medium bg-white border border-[rgba(0,0,0,0.25)] text-[#6b7280] hover:bg-[#f9fafb] cursor-pointer transition-colors"
                @click="closeDialog"
              >取消</button>
              <button
                class="px-[24px] py-[10px] rounded-[24px] text-[14px] font-medium bg-[#2563eb] text-white hover:bg-[#1d4ed8] cursor-pointer transition-colors disabled:opacity-40 disabled:cursor-not-allowed"
                :disabled="!evaluationDimensions.every(d => evalForm[d.name] !== '') || !evalForm.comment?.trim()"
                @click="submitEvaluation"
              >提交评定</button>
            </div>
          </div>
        </div>
      </Teleport>

    </div>
  </LayoutTeacher>
</template>
