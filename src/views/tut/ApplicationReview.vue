<!--
页面编号：TUT-AUD-01
页面名称：实习申请审核（初审）
路由：/teacher/applications
Figma Node：85:15
当前阶段：阶段一-原型还原
-->
<script setup>
import { ref, computed } from 'vue'
import LayoutTeacher from '../../layouts/LayoutTeacher.vue'
import { applicationReviewList, applicationStatusMap } from '../../mock/tut.js'

// 筛选
const activeTab = ref('')
const searchQuery = ref('')

const filteredList = computed(() => {
  let list = applicationReviewList
  if (activeTab.value) {
    list = list.filter(item => item.status === activeTab.value)
  }
  if (searchQuery.value.trim()) {
    const q = searchQuery.value.trim().toLowerCase()
    list = list.filter(item =>
      item.studentName.toLowerCase().includes(q) ||
      item.company.toLowerCase().includes(q) ||
      item.position.toLowerCase().includes(q)
    )
  }
  return list
})

// 审核弹窗
const dialogVisible = ref(false)
const currentItem = ref(null)
const reviewForm = ref({ result: '', opinion: '' })

function openReview(item) {
  currentItem.value = item
  reviewForm.value = { result: '通过', opinion: '' }
  dialogVisible.value = true
}

function closeDialog() {
  dialogVisible.value = false
  currentItem.value = null
}

function submitReview() {
  if (!reviewForm.value.opinion.trim()) return
  // TODO: 实际提交到后端
  currentItem.value.status = reviewForm.value.result === '通过' ? '已通过' : '已驳回'
  closeDialog()
}

// 统计
const stats = computed(() => [
  { label: '全部申请', value: applicationReviewList.length, color: '#2563EB' },
  { label: '待审核', value: applicationReviewList.filter(i => i.status === '待审核').length, color: '#F59E0B' },
  { label: '已通过', value: applicationReviewList.filter(i => i.status === '已通过').length, color: '#16A34A' },
  { label: '已驳回', value: applicationReviewList.filter(i => i.status === '已驳回').length, color: '#FF383C' },
])

const statusTabs = [
  { label: '全部', value: '' },
  { label: '待审核', value: '待审核' },
  { label: '已通过', value: '已通过' },
  { label: '已驳回', value: '已驳回' },
]
</script>

<template>
  <LayoutTeacher>
    <div class="p-[30px_40px]">

      <!-- ===== 页面标题 ===== -->
      <h1 class="text-[32px] font-bold text-black mb-[4px]">实习申请审核</h1>
      <p class="text-[16px] text-[#757575] mb-[24px]">审核学生提交的实习申请，通过后流转至学院复核</p>

      <!-- ===== 统计卡片 ===== -->
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

      <!-- ===== 筛选 + 搜索 ===== -->
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

      <!-- ===== 数据表格 ===== -->
      <div class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] overflow-hidden">
        <div class="grid grid-cols-[100px_1fr_1fr_100px_100px_90px_100px] gap-[12px] px-[20px] py-[14px] bg-[#f9fafb] text-[13px] font-medium text-[#6b7280] border-b border-[#e5e7eb]">
          <div>学生</div>
          <div>企业 / 岗位</div>
          <div>所属学院</div>
          <div>实习类型</div>
          <div>申请时间</div>
          <div>状态</div>
          <div>操作</div>
        </div>

        <div v-if="filteredList.length === 0" class="text-center py-[60px] text-[#9ca3af] text-[14px]">
          暂无匹配的申请记录
        </div>

        <div
          v-for="(item, idx) in filteredList"
          :key="item.id"
          class="grid grid-cols-[100px_1fr_1fr_100px_100px_90px_100px] gap-[12px] px-[20px] py-[16px] text-[14px] items-center hover:bg-[#f9fafb] transition-colors"
          :class="{ 'border-t border-[#f3f4f6]': idx > 0 }"
        >
          <div class="font-medium">{{ item.studentName }}</div>
          <div>
            <div class="font-medium">{{ item.company }}</div>
            <div class="text-[12px] text-[#757575]">{{ item.position }}</div>
          </div>
          <div class="text-[#757575]">{{ item.department }}</div>
          <div>{{ item.type }}</div>
          <div class="text-[#757575]">{{ item.applyDate }}</div>
          <div>
            <span
              class="inline-block px-[10px] py-[2px] rounded-[12px] text-[12px] font-medium"
              :style="{ color: applicationStatusMap[item.status]?.color, background: applicationStatusMap[item.status]?.bg }"
            >{{ item.status }}</span>
          </div>
          <div>
            <button
              v-if="item.status === '待审核'"
              class="text-[#2563eb] text-[13px] font-medium hover:underline cursor-pointer"
              @click="openReview(item)"
            >审核</button>
            <span v-else class="text-[13px] text-[#9ca3af]">已处理</span>
          </div>
        </div>
      </div>

      <!-- ===== 审核弹窗 ===== -->
      <Teleport to="body">
        <div
          v-if="dialogVisible"
          class="fixed inset-0 z-50 flex items-center justify-center"
        >
          <!-- 遮罩 -->
          <div class="absolute inset-0 bg-black/40" @click="closeDialog" />
          <!-- 弹窗 -->
          <div class="relative bg-white rounded-[15px] w-[520px] max-h-[80vh] overflow-auto shadow-xl p-[32px]">
            <h2 class="text-[20px] font-bold mb-[20px]">审核实习申请</h2>

            <!-- 申请信息 -->
            <div v-if="currentItem" class="bg-[#f9fafb] rounded-[8px] p-[16px] mb-[20px] text-[14px] space-y-[6px]">
              <div><span class="text-[#757575]">学生：</span>{{ currentItem.studentName }}（{{ currentItem.studentNo }}）</div>
              <div><span class="text-[#757575]">企业：</span>{{ currentItem.company }}</div>
              <div><span class="text-[#757575]">岗位：</span>{{ currentItem.position }}</div>
              <div><span class="text-[#757575]">类型：</span>{{ currentItem.type }} · {{ currentItem.department }}</div>
            </div>

            <!-- 审核结果 -->
            <div class="mb-[16px]">
              <span class="text-[14px] font-medium mb-[8px] block">审核结果</span>
              <div class="flex gap-[12px]">
                <button
                  class="flex-1 py-[10px] rounded-[8px] text-[14px] font-medium border transition-colors cursor-pointer"
                  :class="reviewForm.result === '通过'
                    ? 'bg-[#16a34a] text-white border-[#16a34a]'
                    : 'bg-white text-[#16a34a] border-[rgba(0,0,0,0.16)] hover:bg-[#f0fdf4]'"
                  @click="reviewForm.result = '通过'"
                >✓ 通过</button>
                <button
                  class="flex-1 py-[10px] rounded-[8px] text-[14px] font-medium border transition-colors cursor-pointer"
                  :class="reviewForm.result === '驳回'
                    ? 'bg-[#ff383c] text-white border-[#ff383c]'
                    : 'bg-white text-[#ff383c] border-[rgba(0,0,0,0.16)] hover:bg-[#fef2f2]'"
                  @click="reviewForm.result = '驳回'"
                >✗ 驳回</button>
              </div>
            </div>

            <!-- 审核意见 -->
            <div class="mb-[24px]">
              <span class="text-[14px] font-medium mb-[8px] block">审核意见 <span class="text-[#ff383c]">*</span></span>
              <textarea
                v-model="reviewForm.opinion"
                placeholder="请输入审核意见..."
                class="w-full h-[100px] bg-white border border-[rgba(0,0,0,0.16)] rounded-[8px] p-[12px] text-[14px] outline-none focus:border-[#2563eb] resize-none placeholder-[#9ca3af]"
              />
            </div>

            <!-- 按钮组 -->
            <div class="flex justify-end gap-[12px]">
              <button
                class="px-[24px] py-[10px] rounded-[24px] text-[14px] font-medium bg-white border border-[rgba(0,0,0,0.25)] text-[#6b7280] hover:bg-[#f9fafb] cursor-pointer transition-colors"
                @click="closeDialog"
              >取消</button>
              <button
                class="px-[24px] py-[10px] rounded-[24px] text-[14px] font-medium bg-[#2563eb] text-white hover:bg-[#1d4ed8] cursor-pointer transition-colors disabled:opacity-40 disabled:cursor-not-allowed"
                :disabled="!reviewForm.opinion.trim()"
                @click="submitReview"
              >确认{{ reviewForm.result }}</button>
            </div>
          </div>
        </div>
      </Teleport>

    </div>
  </LayoutTeacher>
</template>
