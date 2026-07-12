<!--
页面编号：COL-AUD-01
页面名称：企业入驻审核（学院初审）
路由：/college/companies/review
Figma Node：229:15
-->
<script setup>
import { ref, computed } from 'vue'
import LayoutCollege from '../../layouts/LayoutCollege.vue'
import { colCompanyReviewList, colCompanyStatusMap } from '../../mock/col.js'

const activeTab = ref('')
const searchQuery = ref('')

const filteredList = computed(() => {
  let list = colCompanyReviewList
  if (activeTab.value) list = list.filter(i => i.status === activeTab.value)
  if (searchQuery.value.trim()) {
    const q = searchQuery.value.trim().toLowerCase()
    list = list.filter(i => i.name.toLowerCase().includes(q) || i.industry.toLowerCase().includes(q))
  }
  return list
})

const dialogVisible = ref(false)
const currentItem = ref(null)
const reviewForm = ref({ result: '通过', opinion: '' })

function openReview(item) {
  currentItem.value = item
  reviewForm.value = { result: '通过', opinion: '' }
  dialogVisible.value = true
}
function closeDialog() { dialogVisible.value = false; currentItem.value = null }
function submitReview() {
  if (!reviewForm.value.opinion.trim()) return
  currentItem.value.status = reviewForm.value.result === '通过' ? '已通过' : '已驳回'
  closeDialog()
}

const stats = computed(() => [
  { label: '全部企业', value: colCompanyReviewList.length, color: '#2563EB' },
  { label: '待初审', value: colCompanyReviewList.filter(i => i.status === '待初审').length, color: '#F59E0B' },
  { label: '已通过', value: colCompanyReviewList.filter(i => i.status === '已通过').length, color: '#16A34A' },
  { label: '已驳回', value: colCompanyReviewList.filter(i => i.status === '已驳回').length, color: '#FF383C' },
])

const statusTabs = [
  { label: '全部', value: '' },
  { label: '待初审', value: '待初审' },
  { label: '已通过', value: '已通过' },
  { label: '已驳回', value: '已驳回' },
]
</script>

<template>
  <LayoutCollege>
    <div class="p-[30px_40px]">
      <h1 class="text-[32px] font-bold text-black mb-[4px]">企业入驻审核（学院初审）</h1>
      <p class="text-[16px] text-[#757575] mb-[24px]">审核企业入驻申请，通过后流转至学校复核</p>

      <!-- 统计卡片 -->
      <div class="grid grid-cols-4 gap-[22px] mb-[22px]">
        <div v-for="card in stats" :key="card.label"
          class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] p-[20px_24px] flex flex-col items-center justify-center min-h-[110px]">
          <span class="text-[14px] text-[#757575] mb-[4px]">{{ card.label }}</span>
          <span class="text-[32px] font-bold" :style="{ color: card.color }">{{ card.value }}</span>
        </div>
      </div>

      <!-- 筛选 -->
      <div class="flex items-center justify-between mb-[16px]">
        <div class="flex items-center gap-[42px]">
          <button v-for="tab in statusTabs" :key="tab.value"
            class="text-[14px] pb-[8px] border-b-[3px] transition-colors cursor-pointer"
            :class="activeTab === tab.value ? 'text-[#2563eb] border-[#2563eb] font-medium' : 'text-[#757575] border-transparent hover:text-[#374151]'"
            @click="activeTab = tab.value">{{ tab.label }}</button>
        </div>
        <input v-model="searchQuery" placeholder="搜索企业名称或行业..."
          class="w-[260px] px-4 py-2 rounded-[8px] border border-[rgba(0,0,0,0.16)] text-[14px] bg-white focus:outline-none focus:border-[#2563EB]" />
      </div>

      <!-- 表格 -->
      <div class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] overflow-hidden">
        <table class="w-full text-[14px]">
          <thead>
            <tr style="background:#F5F7FB;">
              <th class="text-left px-4 py-3 font-medium text-[#6B7280]">企业名称</th>
              <th class="text-left px-4 py-3 font-medium text-[#6B7280]">行业</th>
              <th class="text-left px-4 py-3 font-medium text-[#6B7280]">规模</th>
              <th class="text-left px-4 py-3 font-medium text-[#6B7280]">联系人</th>
              <th class="text-left px-4 py-3 font-medium text-[#6B7280]">申请日期</th>
              <th class="text-left px-4 py-3 font-medium text-[#6B7280]">状态</th>
              <th class="text-left px-4 py-3 font-medium text-[#6B7280]">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in filteredList" :key="item.id" class="border-b border-gray-100 hover:bg-gray-50">
              <td class="px-4 py-3 font-medium text-gray-900">{{ item.name }}</td>
              <td class="px-4 py-3 text-gray-600">{{ item.industry }}</td>
              <td class="px-4 py-3 text-gray-600">{{ item.scale }}</td>
              <td class="px-4 py-3 text-gray-600">{{ item.contact }}</td>
              <td class="px-4 py-3 text-gray-600">{{ item.applyDate }}</td>
              <td class="px-4 py-3">
                <span class="px-2.5 py-1 rounded-full text-xs font-medium"
                  :style="{ color: colCompanyStatusMap[item.status]?.color || '#6B7280', background: colCompanyStatusMap[item.status]?.bg || 'rgba(107,114,128,0.1)' }">
                  {{ item.status }}
                </span>
              </td>
              <td class="px-4 py-3">
                <button v-if="item.status === '待初审'" @click="openReview(item)"
                  class="text-[14px] font-medium cursor-pointer hover:underline" style="color:#2563EB;">审核</button>
                <span v-else class="text-[14px] text-gray-400">已处理</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 审核弹窗 -->
      <div v-if="dialogVisible" class="fixed inset-0 z-50 flex items-center justify-center bg-black/40" @click.self="closeDialog">
        <div class="bg-white rounded-[15px] w-[500px] p-8 shadow-xl">
          <h3 class="text-[20px] font-bold mb-4">企业入驻审核</h3>
          <div class="text-[14px] space-y-2 mb-6">
            <p><span class="text-gray-500">企业名称：</span><span class="font-medium">{{ currentItem?.name }}</span></p>
            <p><span class="text-gray-500">行业：</span>{{ currentItem?.industry }}</p>
            <p><span class="text-gray-500">联系人：</span>{{ currentItem?.contact }} {{ currentItem?.phone }}</p>
          </div>
          <div class="mb-4">
            <label class="block text-[13px] font-medium mb-2 text-gray-700">审核结果</label>
            <div class="flex gap-4">
              <label class="flex items-center gap-2 text-[14px] cursor-pointer">
                <input type="radio" v-model="reviewForm.result" value="通过" class="accent-[#2563EB]" /> 通过
              </label>
              <label class="flex items-center gap-2 text-[14px] cursor-pointer">
                <input type="radio" v-model="reviewForm.result" value="驳回" class="accent-[#FF383C]" /> 驳回
              </label>
            </div>
          </div>
          <div class="mb-6">
            <label class="block text-[13px] font-medium mb-2 text-gray-700">审核意见</label>
            <textarea v-model="reviewForm.opinion" rows="3" placeholder="请输入审核意见..."
              class="w-full px-4 py-2.5 rounded-[8px] border border-[rgba(0,0,0,0.16)] text-[14px] resize-none focus:outline-none focus:border-[#2563EB]"></textarea>
          </div>
          <div class="flex gap-3 justify-end">
            <button @click="closeDialog"
              class="px-6 py-2 rounded-[8px] border border-[rgba(0,0,0,0.25)] text-[14px] text-[#6B7280] bg-white hover:bg-gray-50">取消</button>
            <button @click="submitReview"
              class="px-6 py-2 rounded-[8px] text-[14px] font-medium text-white hover:opacity-90"
              :style="{ background: reviewForm.result === '通过' ? '#2563EB' : '#FF383C' }">
              {{ reviewForm.result === '通过' ? '通过' : '驳回' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </LayoutCollege>
</template>
