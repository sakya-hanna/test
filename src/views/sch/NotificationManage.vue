<!--
页面编号：SCH-NOTI-01
页面名称：通知公告管理
路由：/school/notifications
Figma Node：114:15
当前阶段：阶段一-原型还原
-->
<script setup>
import { ref, computed } from 'vue'
import LayoutSchool from '../../layouts/LayoutSchool.vue'
import { notificationList, notiStatusMap, notiTypes, notiChannels } from '../../mock/sch.js'

const activeTab = ref('')
const searchQuery = ref('')
const showForm = ref(false)

const form = ref({ title: '', type: '', channel: '', audience: '', content: '' })

function toggleForm() { showForm.value = !showForm.value; if (!showForm.value) resetForm() }
function resetForm() { form.value = { title: '', type: '', channel: '', audience: '', content: '' } }
function publish() {
  if (!form.value.title || !form.value.type) return
  notificationList.unshift({
    id: Date.now(), ...form.value, publishDate: new Date().toISOString().slice(0,10), publisher: '王处长', status: '已发布', readCount: 0
  })
  toggleForm()
}

const filteredList = computed(() => {
  let list = notificationList
  if (activeTab.value) list = list.filter(i => i.status === activeTab.value)
  if (searchQuery.value.trim()) {
    const q = searchQuery.value.trim().toLowerCase()
    list = list.filter(i => i.title.toLowerCase().includes(q) || i.type.includes(q))
  }
  return list
})

const statusTabs = [
  { label: '全部', value: '' },
  { label: '已发布', value: '已发布' },
  { label: '草稿', value: '草稿' },
]
</script>

<template>
  <LayoutSchool>
    <div class="p-[30px_40px]">

      <div class="flex items-start justify-between mb-[24px]">
        <div>
          <h1 class="text-[32px] font-bold text-black mb-[4px]">通知公告管理</h1>
          <p class="text-[16px] text-[#757575]">发布系统通知，支持多渠道推送</p>
        </div>
        <button class="bg-[#2563eb] text-white text-[15px] font-medium px-[24px] py-[10px] rounded-[24px] hover:bg-[#1d4ed8] cursor-pointer" @click="toggleForm">
          {{ showForm ? '取消' : '+ 新建通知' }}
        </button>
      </div>

      <!-- 发布表单 -->
      <div v-if="showForm" class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] p-[24px] mb-[22px]">
        <h2 class="text-[16px] font-semibold mb-[16px]">新建通知公告</h2>
        <div class="grid grid-cols-2 gap-[16px] mb-[16px]">
          <div class="col-span-2">
            <label class="text-[13px] text-[#6b7280] mb-[4px] block">通知标题 <span class="text-[#ff383c]">*</span></label>
            <input v-model="form.title" placeholder="请输入通知标题" class="w-full h-[36px] bg-white border border-[rgba(0,0,0,0.16)] rounded-[8px] px-[12px] text-[14px] outline-none focus:border-[#2563eb] placeholder-[#9ca3af]" />
          </div>
          <div>
            <label class="text-[13px] text-[#6b7280] mb-[4px] block">通知类型 <span class="text-[#ff383c]">*</span></label>
            <select v-model="form.type" class="w-full h-[36px] bg-white border border-[rgba(0,0,0,0.16)] rounded-[8px] px-[12px] text-[14px] outline-none focus:border-[#2563eb]">
              <option value="">请选择</option>
              <option v-for="t in notiTypes" :key="t" :value="t">{{ t }}</option>
            </select>
          </div>
          <div>
            <label class="text-[13px] text-[#6b7280] mb-[4px] block">推送渠道</label>
            <select v-model="form.channel" class="w-full h-[36px] bg-white border border-[rgba(0,0,0,0.16)] rounded-[8px] px-[12px] text-[14px] outline-none focus:border-[#2563eb]">
              <option value="">请选择</option>
              <option v-for="c in notiChannels" :key="c" :value="c">{{ c }}</option>
            </select>
          </div>
          <div>
            <label class="text-[13px] text-[#6b7280] mb-[4px] block">目标受众</label>
            <input v-model="form.audience" placeholder="如：全校师生、2023级学生" class="w-full h-[36px] bg-white border border-[rgba(0,0,0,0.16)] rounded-[8px] px-[12px] text-[14px] outline-none focus:border-[#2563eb] placeholder-[#9ca3af]" />
          </div>
          <div>
            <label class="text-[13px] text-[#6b7280] mb-[4px] block">通知内容</label>
            <textarea v-model="form.content" placeholder="请输入通知正文..." class="w-full h-[80px] bg-white border border-[rgba(0,0,0,0.16)] rounded-[8px] p-[12px] text-[14px] outline-none focus:border-[#2563eb] resize-none placeholder-[#9ca3af]" />
          </div>
        </div>
        <div class="flex justify-end gap-[12px]">
          <button class="px-[20px] py-[8px] rounded-[24px] text-[13px] font-medium bg-white border border-[rgba(0,0,0,0.25)] text-[#6b7280] hover:bg-[#f9fafb] cursor-pointer" @click="resetForm">重置</button>
          <button class="px-[20px] py-[8px] rounded-[24px] text-[13px] font-medium bg-white border border-[#2563eb] text-[#2563eb] hover:bg-[#eff6ff] cursor-pointer" @click="publish">存草稿</button>
          <button class="px-[20px] py-[8px] rounded-[24px] text-[13px] font-medium bg-[#2563eb] text-white hover:bg-[#1d4ed8] cursor-pointer disabled:opacity-40" :disabled="!form.title || !form.type" @click="publish">立即发布</button>
        </div>
      </div>

      <!-- 筛选 + 搜索 -->
      <div class="flex items-center justify-between mb-[16px]">
        <div class="flex items-center gap-[42px]">
          <button v-for="tab in statusTabs" :key="tab.value"
            class="text-[14px] pb-[8px] border-b-[3px] transition-colors cursor-pointer"
            :class="activeTab === tab.value ? 'text-[#2563eb] border-[#2563eb] font-medium' : 'text-[#757575] border-transparent hover:text-[#374151]'"
            @click="activeTab = tab.value">{{ tab.label }}</button>
        </div>
        <input v-model="searchQuery" type="text" placeholder="🔍 搜索通知..."
          class="w-[280px] h-[36px] bg-white border border-[rgba(0,0,0,0.16)] rounded-[8px] px-[12px] text-[14px] outline-none focus:border-[#2563eb] placeholder-[#9ca3af]" />
      </div>
      <div class="border-t border-[#e5e7eb] mb-0" />

      <!-- 表格 -->
      <div class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] overflow-hidden">
        <div class="grid grid-cols-[1fr_100px_120px_120px_100px_100px_90px_90px] gap-[10px] px-[20px] py-[14px] bg-[#f9fafb] text-[13px] font-medium text-[#6b7280] border-b border-[#e5e7eb]">
          <div>标题</div><div>类型</div><div>渠道</div><div>受众</div><div>发布时间</div><div>已读</div><div>状态</div><div>操作</div>
        </div>
        <div v-if="filteredList.length === 0" class="text-center py-[60px] text-[#9ca3af] text-[14px]">暂无通知</div>
        <div v-for="(item, idx) in filteredList" :key="item.id"
          class="grid grid-cols-[1fr_100px_120px_120px_100px_100px_90px_90px] gap-[10px] px-[20px] py-[16px] text-[14px] items-center hover:bg-[#f9fafb] transition-colors"
          :class="{ 'border-t border-[#f3f4f6]': idx > 0 }">
          <div class="font-medium truncate">{{ item.title }}</div>
          <div><span class="text-[12px] px-[6px] py-[1px] rounded-[6px] bg-[rgba(37,99,235,0.08)] text-[#2563eb]">{{ item.type }}</span></div>
          <div class="text-[#757575] text-[13px]">{{ item.channel }}</div>
          <div class="text-[#757575] text-[13px]">{{ item.audience }}</div>
          <div class="text-[#757575]">{{ item.publishDate || '-' }}</div>
          <div><span class="font-medium text-[#2563eb]">{{ item.readCount }}</span> 人</div>
          <div>
            <span class="inline-block px-[10px] py-[2px] rounded-[12px] text-[12px] font-medium"
              :style="{ color: notiStatusMap[item.status]?.color, background: notiStatusMap[item.status]?.bg }">{{ item.status }}</span>
          </div>
          <div class="flex gap-[8px]">
            <button class="text-[#2563eb] text-[12px] font-medium hover:underline cursor-pointer">编辑</button>
            <button class="text-[#ff383c] text-[12px] font-medium hover:underline cursor-pointer">删除</button>
          </div>
        </div>
      </div>

    </div>
  </LayoutSchool>
</template>
