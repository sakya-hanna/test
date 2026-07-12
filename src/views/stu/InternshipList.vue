<!--
页面编号：STU-INTRN-01
页面名称：我的实习列表
路由：/stu/internships
Figma Node：32:59
当前阶段：阶段一-原型还原
-->
<script setup>
import { ref, computed } from 'vue'
import LayoutBackend from '../../layouts/LayoutBackend.vue'
import { internshipList, internshipStats, internshipTypeTabs, internshipStatusMap } from '../../mock/stu.js'
import { useRouter } from 'vue-router'

const router = useRouter()

// 筛选状态
const activeTab = ref('')
const searchQuery = ref('')

// 按类型筛选 + 搜索过滤
const filteredList = computed(() => {
  let list = internshipList
  if (activeTab.value) {
    list = list.filter(item => item.type === activeTab.value)
  }
  if (searchQuery.value.trim()) {
    const q = searchQuery.value.trim().toLowerCase()
    list = list.filter(item =>
      item.company.toLowerCase().includes(q) ||
      item.position.toLowerCase().includes(q) ||
      item.type.toLowerCase().includes(q)
    )
  }
  return list
})

function viewDetail(id) {
  router.push(`/stu/internships/${id}`)
}

function createNew() {
  router.push('/stu/internships/new')
}
</script>

<template>
  <LayoutBackend>
    <div class="p-[30px_40px]">

      <!-- ===== 页面标题（含新建按钮） ===== -->
      <div class="flex items-start justify-between mb-[24px]">
        <div>
          <h1 class="text-[32px] font-bold text-black mb-[4px]">我的实习列表</h1>
          <p class="text-[16px] text-[#757575]">管理你的实习申请与记录</p>
        </div>
        <button
          class="bg-[#2563eb] text-white text-[15px] font-medium px-[24px] py-[10px] rounded-[24px] hover:bg-[#1d4ed8] transition-colors shrink-0"
          @click="createNew"
        >+ 新建申请</button>
      </div>

      <!-- ===== 统计卡片组 ===== -->
      <div class="grid grid-cols-4 gap-[22px] mb-[20px]">
        <div
          v-for="card in internshipStats"
          :key="card.label"
          class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] p-[20px_24px] flex flex-col items-center justify-center min-h-[120px]"
        >
          <span class="text-[14px] text-[#757575] mb-[4px]">{{ card.label }}</span>
          <span class="text-[32px] font-bold" :style="{ color: card.color }">{{ card.value }}</span>
          <span class="text-[13px] text-[#9ca3af]">{{ card.unit }}</span>
        </div>
      </div>

      <!-- ===== 筛选标签 + 搜索 ===== -->
      <div class="flex items-center justify-between mb-[16px]">
        <!-- 标签 -->
        <div class="flex items-center gap-[42px]">
          <button
            v-for="tab in internshipTypeTabs"
            :key="tab.value"
            class="text-[14px] pb-[8px] border-b-[3px] transition-colors cursor-pointer"
            :class="activeTab === tab.value
              ? 'text-[#2563eb] border-[#2563eb] font-medium'
              : 'text-[#757575] border-transparent hover:text-[#374151]'"
            @click="activeTab = tab.value"
          >{{ tab.label }}</button>
        </div>
        <!-- 搜索框 -->
        <div class="relative">
          <input
            v-model="searchQuery"
            type="text"
            placeholder="🔍 搜索实习..."
            class="w-[280px] h-[36px] bg-white border border-[rgba(0,0,0,0.16)] rounded-[8px] px-[12px] text-[14px] outline-none focus:border-[#2563eb] placeholder-[#9ca3af]"
          />
        </div>
      </div>

      <!-- 分隔线 -->
      <div class="border-t border-[#e5e7eb] mb-0" />

      <!-- ===== 数据表格 ===== -->
      <div class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] overflow-hidden">
        <!-- 表头 -->
        <div class="grid grid-cols-[120px_1fr_1fr_120px_100px_80px] gap-[16px] px-[20px] py-[14px] bg-[#f9fafb] text-[13px] font-medium text-[#6b7280] border-b border-[#e5e7eb]">
          <div>实习类型</div>
          <div>企业名称</div>
          <div>实习岗位</div>
          <div>申请时间</div>
          <div>状态</div>
          <div>操作</div>
        </div>

        <!-- 空状态 -->
        <div v-if="filteredList.length === 0" class="text-center py-[60px] text-[#9ca3af] text-[14px]">
          暂无匹配的实习记录
        </div>

        <!-- 数据行 -->
        <div
          v-for="(item, idx) in filteredList"
          :key="item.id"
          class="grid grid-cols-[120px_1fr_1fr_120px_100px_80px] gap-[16px] px-[20px] py-[16px] text-[14px] items-center hover:bg-[#f9fafb] transition-colors"
          :class="{ 'border-t border-[#f3f4f6]': idx > 0 }"
        >
          <div>{{ item.type }}</div>
          <div class="font-medium">{{ item.company }}</div>
          <div>{{ item.position }}</div>
          <div class="text-[#757575]">{{ item.applyDate }}</div>
          <div>
            <span
              class="inline-block px-[10px] py-[2px] rounded-[12px] text-[12px] font-medium"
              :style="{
                color: (internshipStatusMap[item.status] || {}).color || '#6b7280',
                background: (internshipStatusMap[item.status] || {}).bg || 'rgba(0,0,0,0.05)',
              }"
            >{{ item.status }}</span>
          </div>
          <div>
            <button
              class="text-[#2563eb] text-[13px] font-medium hover:underline cursor-pointer"
              @click="viewDetail(item.id)"
            >查看</button>
          </div>
        </div>
      </div>

    </div>
  </LayoutBackend>
</template>
