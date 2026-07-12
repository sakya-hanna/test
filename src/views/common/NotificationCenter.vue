<!--
页面编号：COM-NOTI-01
页面名称：通用消息/通知中心模板
Figma Node：196:15
当前阶段：阶段一-原型还原

复用方式：各角色消息中心直接引用此组件，包裹在自己的 Layout 中。
  <LayoutXxx><NotificationCenter :messages="data" /></LayoutXxx>
-->
<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  messages: {
    type: Array,
    default: () => [],
    // { id, title, from, time, read, content, type, attachment }
  },
  emptyText: { type: String, default: '暂无消息' },
})

const emit = defineEmits(['delete', 'mark-read'])

const activeId = ref(null)
const activeMsg = ref(null)

const unreadCount = computed(() => props.messages.filter(m => !m.read).length)

function select(msg) {
  activeId.value = msg.id
  activeMsg.value = msg
  msg.read = true
}

function markAllRead() {
  props.messages.forEach(m => { m.read = true })
  activeMsg.value = null
  activeId.value = null
}

function deleteMsg(msg) {
  const idx = props.messages.findIndex(m => m.id === msg.id)
  if (idx > -1) {
    const deleted = props.messages.splice(idx, 1)[0]
    if (activeId.value === deleted.id) {
      activeId.value = null
      activeMsg.value = null
    }
  }
}

function getTypeTag(type) {
  const map = {
    audit: { text: '审核', bg: '#e6f7ff', color: '#1890ff' },
    alert: { text: '预警', bg: '#fff7e6', color: '#fa8c16' },
    report: { text: '周报', bg: '#f6ffed', color: '#52c41a' },
    notice: { text: '公告', bg: '#f9f0ff', color: '#722ed1' },
    system: { text: '系统', bg: '#f0f0f0', color: '#666' },
  }
  return map[type] || null
}
</script>

<template>
  <div class="flex gap-[22px] h-full">
    <!-- 左侧消息列表 -->
    <div class="w-[360px] shrink-0 bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] overflow-hidden flex flex-col">
      <!-- 列表头 -->
      <div class="px-[20px] py-[14px] border-b border-[#e5e7eb] flex items-center justify-between">
        <span class="text-[15px] font-semibold">消息列表</span>
        <span class="text-[12px]" :class="unreadCount > 0 ? 'text-[#ff4d4f]' : 'text-[#9ca3af]'">
          <span v-if="unreadCount > 0" class="inline-block w-[8px] h-[8px] rounded-full bg-[#ff4d4f] mr-[4px]"></span>
          {{ unreadCount > 0 ? unreadCount + ' 条未读' : '全部已读' }}
        </span>
      </div>
      <!-- 工具栏 -->
      <div class="px-[20px] py-[10px] border-b border-[#f3f4f6] flex items-center gap-[8px]">
        <button
          v-if="unreadCount > 0"
          class="h-[33px] px-[16px] border border-[#1890ff] bg-white text-[#1890ff] rounded-[4px] text-[13px] cursor-pointer hover:bg-[#1890ff] hover:text-white transition-colors"
          @click="markAllRead"
        >
          全部已读
        </button>
        <span v-else class="text-[12px] text-[#9ca3af]">所有消息已读</span>
      </div>
      <!-- 列表体 -->
      <div class="flex-1 overflow-auto">
        <div v-if="messages.length === 0" class="flex items-center justify-center h-full text-[#9ca3af] text-[14px]">
          {{ emptyText }}
        </div>
        <div
          v-for="(msg, idx) in messages"
          :key="msg.id"
          class="p-[16px_20px] cursor-pointer hover:bg-[#f9fafb] transition-colors relative"
          :class="[
            activeId === msg.id ? 'bg-[#eff6ff] border-l-[3px] border-[#1890ff]' : 'border-l-[3px] border-transparent',
            idx > 0 ? 'border-t border-[#f3f4f6]' : ''
          ]"
          @click="select(msg)"
        >
          <div class="flex items-start gap-[12px]">
            <div
              class="w-[38px] h-[38px] rounded-full shrink-0 flex items-center justify-center text-[14px] text-white font-semibold"
              :class="msg.read ? 'bg-[#d1d5db]' : 'bg-[#1890ff]'"
            >
              {{ (msg.title || '通')[0] }}
            </div>
            <div class="min-w-0 flex-1">
              <div v-if="getTypeTag(msg.type)" class="mb-[2px]">
                <span class="inline-block text-[11px] px-[6px] py-[1px] rounded-[2px]"
                  :style="{ background: getTypeTag(msg.type).bg, color: getTypeTag(msg.type).color }">
                  {{ getTypeTag(msg.type).text }}
                </span>
              </div>
              <div class="text-[14px] font-medium truncate" :class="msg.read ? 'text-[#6b7280]' : 'text-black'">
                {{ msg.title }}
              </div>
              <div class="text-[12px] text-[#9ca3af] mt-[4px] truncate">
                {{ msg.from }} · {{ msg.time }}
              </div>
            </div>
            <span
              v-if="!msg.read"
              class="w-[8px] h-[8px] rounded-full shrink-0 mt-[16px]"
              :class="activeId === msg.id ? 'bg-[#1890ff]' : 'bg-[#ff4d4f]'"
            />
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧消息详情 -->
    <div class="flex-1 bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] p-[32px] flex flex-col">
      <template v-if="activeMsg">
        <div class="flex items-start justify-between mb-[4px]">
          <div v-if="getTypeTag(activeMsg.type)" class="flex gap-[6px]">
            <span class="inline-block text-[12px] px-[8px] py-[2px] rounded-[4px]"
              :style="{ background: getTypeTag(activeMsg.type).bg, color: getTypeTag(activeMsg.type).color }">
              {{ getTypeTag(activeMsg.type).text }}
            </span>
          </div>
          <div class="flex gap-[8px] ml-auto">
            <button
              class="h-[33px] px-[14px] border border-[#d9d9d9] bg-white text-[#666] rounded-[4px] text-[13px] cursor-pointer hover:text-[#1890ff] hover:border-[#1890ff] transition-colors"
              @click="select(activeMsg)"
            >
              标记已读
            </button>
            <button
              class="h-[33px] px-[14px] border border-[#ff4d4f] bg-white text-[#ff4d4f] rounded-[4px] text-[13px] cursor-pointer hover:bg-[#ff4d4f] hover:text-white transition-colors"
              @click="deleteMsg(activeMsg)"
            >
              删除
            </button>
          </div>
        </div>
        <h2 class="text-[20px] font-bold mt-[8px] mb-[8px]">{{ activeMsg.title }}</h2>
        <div class="flex items-center gap-[16px] text-[13px] text-[#9ca3af] mb-[20px]">
          <span>发布主体：{{ activeMsg.from }}</span>
          <span>时间：{{ activeMsg.time }}</span>
        </div>
        <div class="border-t border-[#e5e7eb] pt-[20px] flex-1">
          <div class="text-[15px] text-[#374151] leading-relaxed whitespace-pre-wrap">
            {{ activeMsg.content || '（暂无详细内容）' }}
          </div>
          <!-- 附件 -->
          <div
            v-if="activeMsg.attachment"
            class="mt-[20px] flex items-center gap-[10px] p-[10px_14px] bg-[#fafafa] border border-[#e8e8e8] rounded-[6px] cursor-pointer hover:border-[#1890ff] hover:bg-[#f0f8ff] transition-colors"
          >
            <span class="text-[20px]">📄</span>
            <div class="flex-1">
              <div class="text-[13px] text-[#333]">{{ activeMsg.attachment.name }}</div>
              <div class="text-[11px] text-[#999]">{{ activeMsg.attachment.size }}</div>
            </div>
            <span class="text-[#1890ff] text-[16px]">⬇</span>
          </div>
        </div>
      </template>
      <template v-else>
        <div class="flex items-center justify-center h-full text-[#9ca3af] text-[14px]">
          👈 选择左侧消息查看详情
        </div>
      </template>
    </div>
  </div>
</template>
