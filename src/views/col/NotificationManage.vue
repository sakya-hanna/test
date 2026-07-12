<!--
页面编号：COL-NOTI-01
页面名称：学院通知公告发布
路由：/college/notifications
Figma Node：263:15
-->
<script setup>
import { ref, reactive } from 'vue'
import LayoutCollege from '../../layouts/LayoutCollege.vue'

const publishForm = reactive({ title: '', content: '' })
const activeTab = ref('')

const notifications = ref([
  { id: 1, title: '关于规范实习申请审核流程的通知', time: '2026-06-20 10:00', status: '已发布', views: 45 },
  { id: 2, title: '6月实习数据汇总提交提醒', time: '2026-06-15 14:30', status: '已发布', views: 38 },
  { id: 3, title: '企业入驻审核注意事项', time: '2026-06-10 09:00', status: '已发布', views: 52 },
  { id: 4, title: '暑期实习安全教育工作安排（草稿）', time: '2026-06-25 16:00', status: '草稿', views: 0 },
])

const filteredList = ref(notifications.value)

function applyFilter() {
  filteredList.value = activeTab.value
    ? notifications.value.filter(n => n.status === activeTab.value)
    : notifications.value
}

function publish() {
  if (!publishForm.title.trim() || !publishForm.content.trim()) return
  notifications.value.unshift({ id: Date.now(), title: publishForm.title, time: new Date().toLocaleString('zh-CN'), status: '已发布', views: 0 })
  publishForm.title = ''
  publishForm.content = ''
  applyFilter()
}

const statusTabs = [
  { label: '全部', value: '' },
  { label: '已发布', value: '已发布' },
  { label: '草稿', value: '草稿' },
]

const statusStyle = {
  '已发布': { color: '#16A34A', bg: 'rgba(20,141,61,0.1)' },
  '草稿': { color: '#6B7280', bg: 'rgba(107,114,128,0.1)' },
}
</script>

<template>
  <LayoutCollege>
    <div class="p-[30px_40px]">
      <h1 class="text-[32px] font-bold text-black mb-[4px]">通知公告发布</h1>
      <p class="text-[16px] text-[#757575] mb-[24px]">学院通知公告管理与发布</p>

      <!-- 发布表单 -->
      <div class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] p-6 mb-6">
        <h3 class="text-[18px] font-semibold mb-4">发布新通知</h3>
        <input v-model="publishForm.title" placeholder="通知标题"
          class="w-full px-4 py-2.5 rounded-[8px] border border-[rgba(0,0,0,0.16)] text-[14px] mb-3 focus:outline-none focus:border-[#2563EB]" />
        <textarea v-model="publishForm.content" rows="3" placeholder="通知内容..."
          class="w-full px-4 py-2.5 rounded-[8px] border border-[rgba(0,0,0,0.16)] text-[14px] resize-none mb-3 focus:outline-none focus:border-[#2563EB]"></textarea>
        <div class="flex gap-3">
          <button @click="publish"
            class="px-6 py-2 rounded-[24px] text-[14px] font-medium text-white hover:opacity-90" style="background:#2563EB;">发布通知</button>
          <span class="text-[12px] text-[#9CA3AF] self-center">原型展示 — 发布后刷新列表</span>
        </div>
      </div>

      <!-- 已发布列表 -->
      <div class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)]">
        <div class="px-6 py-4 border-b">
          <div class="flex items-center gap-[42px]">
            <button v-for="tab in statusTabs" :key="tab.value"
              class="text-[14px] pb-[8px] border-b-[3px] transition-colors cursor-pointer"
              :class="activeTab === tab.value ? 'text-[#2563eb] border-[#2563eb] font-medium' : 'text-[#757575] border-transparent hover:text-[#374151]'"
              @click="activeTab = tab.value; applyFilter()">{{ tab.label }}</button>
          </div>
        </div>
        <table class="w-full text-[14px]">
          <thead>
            <tr style="background:#F5F7FB;">
              <th class="text-left px-4 py-3 font-medium text-[#6B7280]">标题</th>
              <th class="text-left px-4 py-3 font-medium text-[#6B7280]">发布时间</th>
              <th class="text-left px-4 py-3 font-medium text-[#6B7280]">状态</th>
              <th class="text-left px-4 py-3 font-medium text-[#6B7280]">阅读量</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="filteredList.length === 0">
              <td colspan="4" class="px-4 py-12 text-center text-[#9CA3AF]">
                暂无{{ activeTab || '' }}通知 — <span class="text-[#2563EB] cursor-pointer" @click="activeTab = ''; applyFilter()">查看全部</span>
              </td>
            </tr>
            <tr v-for="n in filteredList" :key="n.id" class="border-b border-gray-100 hover:bg-gray-50">
              <td class="px-4 py-3 font-medium text-gray-900">{{ n.title }}</td>
              <td class="px-4 py-3 text-gray-500">{{ n.time }}</td>
              <td class="px-4 py-3">
                <span class="px-2.5 py-1 rounded-full text-xs font-medium"
                  :style="{ color: statusStyle[n.status]?.color, background: statusStyle[n.status]?.bg }">{{ n.status }}</span>
              </td>
              <td class="px-4 py-3 text-gray-500">{{ n.views }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </LayoutCollege>
</template>
