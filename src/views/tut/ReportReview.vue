<!--
页面编号：TUT-RPT-01
页面名称：周报月报批阅
路由：/teacher/reports
Figma Node：98:15
当前阶段：阶段一-原型还原
-->
<script setup>
import { ref, computed } from 'vue'
import LayoutTeacher from '../../layouts/LayoutTeacher.vue'
import { reportReviewList, reportStatusMap } from '../../mock/tut.js'

// 筛选
const activeTab = ref('')
const searchQuery = ref('')

const filteredList = computed(() => {
  let list = reportReviewList
  if (activeTab.value) {
    list = list.filter(item => item.status === activeTab.value)
  }
  if (searchQuery.value.trim()) {
    const q = searchQuery.value.trim().toLowerCase()
    list = list.filter(item =>
      item.studentName.toLowerCase().includes(q) ||
      item.title.toLowerCase().includes(q)
    )
  }
  return list
})

// 批阅弹窗
const dialogVisible = ref(false)
const currentItem = ref(null)
const reviewForm = ref({ rating: '', comment: '' })

function openReview(item) {
  currentItem.value = item
  reviewForm.value = { rating: '良好', comment: '' }
  dialogVisible.value = true
}

function closeDialog() {
  dialogVisible.value = false
  currentItem.value = null
}

function submitReview() {
  if (!reviewForm.value.comment.trim()) return
  currentItem.value.status = '已批阅'
  closeDialog()
}

const ratingOptions = ['优秀', '良好', '合格', '不合格']

const stats = computed(() => [
  { label: '全部报告', value: reportReviewList.length, color: '#2563EB' },
  { label: '待批阅', value: reportReviewList.filter(i => i.status === '待批阅').length, color: '#F59E0B' },
  { label: '已批阅', value: reportReviewList.filter(i => i.status === '已批阅').length, color: '#16A34A' },
  { label: '本周提交', value: 4, color: '#7C3AED' },
])

const statusTabs = [
  { label: '全部', value: '' },
  { label: '待批阅', value: '待批阅' },
  { label: '已批阅', value: '已批阅' },
]
</script>

<template>
  <LayoutTeacher>
    <div class="p-[30px_40px]">

      <h1 class="text-[32px] font-bold text-black mb-[4px]">周报月报批阅</h1>
      <p class="text-[16px] text-[#757575] mb-[24px]">批阅学生提交的实习周报与月报</p>

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
          placeholder="🔍 搜索学生、报告..."
          class="w-[280px] h-[36px] bg-white border border-[rgba(0,0,0,0.16)] rounded-[8px] px-[12px] text-[14px] outline-none focus:border-[#2563eb] placeholder-[#9ca3af]"
        />
      </div>

      <div class="border-t border-[#e5e7eb] mb-0" />

      <!-- 数据表格 -->
      <div class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] overflow-hidden">
        <div class="grid grid-cols-[100px_80px_1fr_120px_90px_80px_90px] gap-[12px] px-[20px] py-[14px] bg-[#f9fafb] text-[13px] font-medium text-[#6b7280] border-b border-[#e5e7eb]">
          <div>学生</div>
          <div>类型</div>
          <div>报告标题</div>
          <div>提交时间</div>
          <div>字数</div>
          <div>状态</div>
          <div>操作</div>
        </div>

        <div v-if="filteredList.length === 0" class="text-center py-[60px] text-[#9ca3af] text-[14px]">
          暂无匹配的报告记录
        </div>

        <div
          v-for="(item, idx) in filteredList"
          :key="item.id"
          class="grid grid-cols-[100px_80px_1fr_120px_90px_80px_90px] gap-[12px] px-[20px] py-[16px] text-[14px] items-center hover:bg-[#f9fafb] transition-colors"
          :class="{ 'border-t border-[#f3f4f6]': idx > 0 }"
        >
          <div class="font-medium">{{ item.studentName }}</div>
          <div>
            <span class="text-[12px] px-[6px] py-[1px] rounded-[6px]"
              :class="item.type === '周报' ? 'text-[#2563eb] bg-[rgba(37,99,235,0.08)]' : 'text-[#7c3aed] bg-[rgba(124,58,237,0.08)]'"
            >{{ item.type }}</span>
          </div>
          <div class="font-medium">{{ item.title }}</div>
          <div class="text-[#757575]">{{ item.submitDate }}</div>
          <div class="text-[#757575]">{{ item.wordCount }}字</div>
          <div>
            <span
              class="inline-block px-[10px] py-[2px] rounded-[12px] text-[12px] font-medium"
              :style="{ color: reportStatusMap[item.status]?.color, background: reportStatusMap[item.status]?.bg }"
            >{{ item.status }}</span>
          </div>
          <div>
            <button
              v-if="item.status === '待批阅'"
              class="text-[#2563eb] text-[13px] font-medium hover:underline cursor-pointer"
              @click="openReview(item)"
            >批阅</button>
            <span v-else class="text-[13px] text-[#9ca3af]">已批阅</span>
          </div>
        </div>
      </div>

      <!-- 批阅弹窗 -->
      <Teleport to="body">
        <div v-if="dialogVisible" class="fixed inset-0 z-50 flex items-center justify-center">
          <div class="absolute inset-0 bg-black/40" @click="closeDialog" />
          <div class="relative bg-white rounded-[15px] w-[520px] max-h-[80vh] overflow-auto shadow-xl p-[32px]">
            <h2 class="text-[20px] font-bold mb-[20px]">批阅报告</h2>

            <div v-if="currentItem" class="bg-[#f9fafb] rounded-[8px] p-[16px] mb-[20px] text-[14px] space-y-[6px]">
              <div><span class="text-[#757575]">学生：</span>{{ currentItem.studentName }}（{{ currentItem.studentNo }}）</div>
              <div><span class="text-[#757575]">报告：</span>{{ currentItem.title }}</div>
              <div><span class="text-[#757575]">提交：</span>{{ currentItem.submitDate }} · {{ currentItem.wordCount }}字</div>
            </div>

            <!-- 评级 -->
            <div class="mb-[16px]">
              <span class="text-[14px] font-medium mb-[8px] block">评级</span>
              <div class="flex gap-[8px]">
                <button
                  v-for="r in ratingOptions"
                  :key="r"
                  class="px-[16px] py-[8px] rounded-[20px] text-[13px] font-medium border transition-colors cursor-pointer"
                  :class="reviewForm.rating === r
                    ? 'bg-[#2563eb] text-white border-[#2563eb]'
                    : 'bg-white text-[#6b7280] border-[rgba(0,0,0,0.16)] hover:bg-[#f9fafb]'"
                  @click="reviewForm.rating = r"
                >{{ r }}</button>
              </div>
            </div>

            <!-- 批阅意见 -->
            <div class="mb-[24px]">
              <span class="text-[14px] font-medium mb-[8px] block">批阅意见 <span class="text-[#ff383c]">*</span></span>
              <textarea
                v-model="reviewForm.comment"
                placeholder="请输入批阅意见与建议..."
                class="w-full h-[120px] bg-white border border-[rgba(0,0,0,0.16)] rounded-[8px] p-[12px] text-[14px] outline-none focus:border-[#2563eb] resize-none placeholder-[#9ca3af]"
              />
            </div>

            <div class="flex justify-end gap-[12px]">
              <button
                class="px-[24px] py-[10px] rounded-[24px] text-[14px] font-medium bg-white border border-[rgba(0,0,0,0.25)] text-[#6b7280] hover:bg-[#f9fafb] cursor-pointer transition-colors"
                @click="closeDialog"
              >取消</button>
              <button
                class="px-[24px] py-[10px] rounded-[24px] text-[14px] font-medium bg-[#2563eb] text-white hover:bg-[#1d4ed8] cursor-pointer transition-colors disabled:opacity-40 disabled:cursor-not-allowed"
                :disabled="!reviewForm.comment.trim()"
                @click="submitReview"
              >提交批阅</button>
            </div>
          </div>
        </div>
      </Teleport>

    </div>
  </LayoutTeacher>
</template>
