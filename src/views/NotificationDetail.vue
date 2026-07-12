<!--
页面编号：COM-NOTI-03
页面名称：通知详情页
路由：/notifications/:id
Figma 参照：COM-NOTI-01 (196:15) 右详情面板 + 设计原型 COM-NOTI-03
用途：独立访问通知详情、支持上下条导航、分享链接、删除操作
-->
<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const allMessages = ref([
  {
    id: 1, title: '关于2026届毕业生实习工作安排的通知', from: '教务处', time: '2026-06-15 09:00', type: 'notice',
    read: false, role: 'school',
    content: '各学院：为做好2026届毕业生实习工作，现将有关事项通知如下：\n\n实习时间安排：2026年7月至2027年6月。各学院应根据专业培养方案要求，合理制定实习计划，确保实习质量。\n\n请各学院相关负责人在系统中完成实习计划的上报和审核工作，审核通过后方可发布给学生。',
    attachment: { name: '2026届毕业实习工作安排细则.pdf', size: '1.2 MB' }
  },
  {
    id: 2, title: '暑假实习安全管理须知', from: '保卫处', time: '2026-06-20 14:30', type: 'alert',
    read: false, role: 'school',
    content: '暑假期间，请各学院加强对实习学生的安全教育和管理，具体要求如下：\n\n1. 加强交通安全教育，提醒学生遵守交通规则\n2. 注意实习场所安全，遵守企业安全规章制度\n3. 做好防暑降温工作，合理安排实习时间\n4. 保持通讯畅通，遇到问题及时向辅导员报告\n\n如遇紧急情况，请拨打保卫处24小时值班电话。'
  },
  {
    id: 3, title: '关于企业入驻审核流程调整的通知', from: '教务处', time: '2026-06-12 11:00', type: 'system',
    read: true, role: 'school',
    content: '为进一步优化企业入驻审核流程，提高审核效率，现对审核流程进行如下调整：\n\n1. 企业注册后由学院进行初审\n2. 初审通过后自动流转至学校复核\n3. 审核周期原则上不超过5个工作日\n\n请各学院管理员和学校管理员知悉并遵照执行。如有疑问请联系教务处实践教学科。'
  },
  {
    id: 4, title: '关于启用新版实习管理系统的通知', from: '系统管理员', time: '2026-06-01 08:00', type: 'system',
    read: true, role: 'school',
    content: '新版实习管理系统已于6月1日正式上线，请各用户通过统一身份认证登录后使用。\n\n新系统包含学生端、教师端、企业端、学院管理端、学校管理端、辅导员端共六个角色端口，实现了实习全流程的数字化管理。\n\n主要功能包括：\n- 实习申请与审核（学生→指导教师→学院复核）\n- 周报月报提交与批阅\n- 成绩评定与查看（企业评价+学校评分）\n- 企业注册与资质审核\n- 实习过程跟踪与安全预警'
  },
])

const notice = ref(null)
const hasPrev = ref(false)
const hasNext = ref(false)

onMounted(() => {
  const id = Number(route.params.id)
  notice.value = allMessages.value.find(m => m.id === id) || null
  markAsRead(id)
})

function markAsRead(id) {
  const m = allMessages.value.find(x => x.id === id)
  if (m) m.read = true
}

const currentIndex = computed(() => {
  if (!notice.value) return -1
  return allMessages.value.findIndex(m => m.id === notice.value.id)
})

const prevMsg = computed(() => {
  if (currentIndex.value > 0) return allMessages.value[currentIndex.value - 1]
  return null
})

const nextMsg = computed(() => {
  if (currentIndex.value < allMessages.value.length - 1) return allMessages.value[currentIndex.value + 1]
  return null
})

function goBack() {
  router.push('/notifications').catch(() => {})
}

function goToPrev() {
  if (prevMsg.value) {
    router.push(`/notifications/${prevMsg.value.id}`).catch(() => {})
    notice.value = prevMsg.value
    markAsRead(prevMsg.value.id)
  }
}

function goToNext() {
  if (nextMsg.value) {
    router.push(`/notifications/${nextMsg.value.id}`).catch(() => {})
    notice.value = nextMsg.value
    markAsRead(nextMsg.value.id)
  }
}

function deleteNotice() {
  if (notice.value) {
    const idx = allMessages.value.findIndex(m => m.id === notice.value.id)
    if (idx > -1) allMessages.value.splice(idx, 1)
    router.push('/notifications').catch(() => {})
  }
}

function getTypeTag(type) {
  const map = {
    notice: { text: '公告', bg: '#f9f0ff', color: '#722ed1' },
    alert:  { text: '安全提醒', bg: '#fff7e6', color: '#fa8c16' },
    system: { text: '系统通知', bg: '#e6f7ff', color: '#1890ff' },
    audit:  { text: '审核通知', bg: '#f6ffed', color: '#52c41a' },
  }
  return map[type] || { text: '通知', bg: '#f0f0f0', color: '#666' }
}
</script>

<template>
  <div class="flex min-h-screen">
    <!-- 侧边栏 — 与 cou/Dashboard.vue 完全一致，通知公告选中 -->
    <aside class="w-[232px] min-h-screen flex-shrink-0 text-white flex flex-col" style="background:#00203D;">
      <div class="px-5 py-5 border-b border-white/10 text-lg font-semibold">实习管理系统</div>
      <nav class="flex-1 py-3 px-3 space-y-1">
        <router-link to="/counselor/dashboard" class="flex items-center px-4 py-3 rounded-lg text-[16px] text-white/80 hover:bg-white/10 transition-colors">指导工作台</router-link>
        <router-link to="/counselor/students" class="flex items-center px-4 py-3 rounded-lg text-[16px] text-white/80 hover:bg-white/10 transition-colors">我的学生</router-link>
        <a href="#" class="flex items-center px-4 py-3 rounded-lg text-[16px] text-white/80 hover:bg-white/10 transition-colors">过程反馈</a>
        <a href="#" class="flex items-center px-4 py-3 rounded-lg text-[16px] text-white/80 hover:bg-white/10 transition-colors">学生评价</a>
        <span class="flex items-center px-4 py-3 rounded-lg text-[16px]" style="color:#C7D2FE;">通知公告</span>
      </nav>
    </aside>

    <!-- 主区域 -->
    <div class="flex-1 flex flex-col min-w-0" style="background:#F5F7FB;">
      <header class="h-[72px] bg-white flex items-center justify-between px-6 shadow-sm flex-shrink-0 border-b border-[#e8e8e8]">
        <div class="flex items-center gap-[16px]">
          <button
            class="flex items-center gap-[6px] text-[14px] text-[#333] cursor-pointer border-none bg-none hover:text-[#1890ff] transition-colors"
            @click="goBack"
          >
            <span class="text-[18px]">←</span>
            <span>返回消息列表</span>
          </button>
          <div class="w-[1px] h-[24px] bg-[#e8e8e8]"></div>
          <span class="text-[14px]" style="color:#757575;">消息中心 / 通知详情</span>
        </div>
        <div class="flex items-center gap-[8px]">
          <button
            class="h-[32px] px-[14px] border border-[#d9d9d9] bg-white text-[#666] rounded-[4px] text-[13px] cursor-pointer hover:text-[#1890ff] hover:border-[#1890ff] transition-colors"
            :disabled="!prevMsg"
            :style="{ opacity: prevMsg ? 1 : 0.4, cursor: prevMsg ? 'pointer' : 'not-allowed' }"
            @click="goToPrev"
          >← 上一条</button>
          <button
            class="h-[32px] px-[14px] border border-[#d9d9d9] bg-white text-[#666] rounded-[4px] text-[13px] cursor-pointer hover:text-[#1890ff] hover:border-[#1890ff] transition-colors"
            :disabled="!nextMsg"
            :style="{ opacity: nextMsg ? 1 : 0.4, cursor: nextMsg ? 'pointer' : 'not-allowed' }"
            @click="goToNext"
          >下一条 →</button>
          <button
            class="h-[32px] px-[14px] bg-[#1890ff] text-white border-none rounded-[4px] text-[13px] cursor-pointer hover:bg-[#40a9ff] transition-colors"
            @click="notice && markAsRead(notice.id)"
          >✓ 标记已读</button>
          <button
            class="h-[32px] px-[14px] border border-[#ff4d4f] bg-white text-[#ff4d4f] rounded-[4px] text-[13px] cursor-pointer hover:bg-[#ff4d4f] hover:text-white transition-colors"
            @click="deleteNotice"
          >🗑 删除</button>
        </div>
      </header>

      <!-- 内容 -->
      <main class="flex-1 overflow-auto p-6">
        <div v-if="notice" class="max-w-[900px] mx-auto">
          <!-- 通知详情卡片 -->
          <div class="bg-white rounded-[8px] overflow-hidden shadow-sm">
            <!-- 卡片头部 -->
            <div class="p-[24px_28px_16px] border-b border-[#f0f0f0]">
              <div class="flex gap-[8px] mb-[12px]">
                <span class="inline-block text-[12px] px-[10px] py-[2px] rounded-[4px]"
                  :style="{ background: getTypeTag(notice.type).bg, color: getTypeTag(notice.type).color }">
                  {{ getTypeTag(notice.type).text }}
                </span>
              </div>
              <h2 class="text-[20px] font-semibold text-[#1a1a1a] mb-[10px] leading-[1.4]">{{ notice.title }}</h2>
              <div class="flex flex-wrap gap-[16px] text-[12px] text-[#999]">
                <span>发布主体：{{ notice.from }}</span>
                <span>发布时间：{{ notice.time }}</span>
                <span>通知编号：#NOTI-{{ String(notice.id).padStart(4, '0') }}</span>
              </div>
            </div>

            <!-- 卡片正文 -->
            <div class="p-[24px_28px]">
              <div class="text-[14px] text-[#333] leading-[1.9] whitespace-pre-wrap">
                {{ notice.content }}
              </div>

              <!-- 附件 -->
              <div v-if="notice.attachment" class="mt-[20px]">
                <div class="text-[13px] font-medium text-[#666] mb-[8px]">📎 相关附件</div>
                <div
                  class="flex items-center gap-[10px] p-[10px_14px] bg-[#fafafa] border border-[#e8e8e8] rounded-[6px] cursor-pointer hover:border-[#1890ff] hover:bg-[#f0f8ff] transition-colors"
                >
                  <span class="text-[22px]">📄</span>
                  <div class="flex-1">
                    <div class="text-[13px] text-[#333]">{{ notice.attachment.name }}</div>
                    <div class="text-[11px] text-[#999]">{{ notice.attachment.size }}</div>
                  </div>
                  <span class="text-[#1890ff] text-[16px]">⬇</span>
                </div>
              </div>
            </div>

            <!-- 卡片底部 -->
            <div class="p-[16px_28px] border-t border-[#f0f0f0] flex justify-between items-center">
              <div class="flex gap-[8px]">
                <button
                  class="h-[35px] px-[18px] bg-[#1890ff] text-white border-none rounded-[4px] text-[13px] cursor-pointer hover:bg-[#40a9ff] transition-colors flex items-center gap-[6px]"
                  @click="notice && markAsRead(notice.id)"
                >✓ 标记已读</button>
                <button
                  class="h-[35px] px-[18px] border border-[#d9d9d9] bg-white text-[#666] rounded-[4px] text-[13px] cursor-pointer hover:text-[#1890ff] hover:border-[#1890ff] transition-colors"
                  @click="goBack"
                >返回列表</button>
              </div>
              <button
                class="h-[35px] px-[18px] border border-[#ff4d4f] bg-white text-[#ff4d4f] rounded-[4px] text-[13px] cursor-pointer hover:bg-[#ff4d4f] hover:text-white transition-colors"
                @click="deleteNotice"
              >🗑 删除此通知</button>
            </div>
          </div>

          <!-- 上/下一条导航 -->
          <div class="flex gap-[12px] mt-[16px]">
            <div
              class="flex-1 p-[12px_16px] bg-white rounded-[8px] border border-[#e8e8e8] cursor-pointer hover:border-[#1890ff] hover:shadow-[0_2px_8px_rgba(24,144,255,0.1)] transition-all"
              :class="{ 'opacity-40 cursor-not-allowed': !prevMsg }"
              @click="goToPrev"
            >
              <div class="text-[12px] text-[#999]">← 上一条</div>
              <div class="text-[13px] text-[#333] mt-[2px] truncate">{{ prevMsg ? prevMsg.title : '没有更早的通知' }}</div>
            </div>
            <div
              class="flex-1 p-[12px_16px] bg-white rounded-[8px] border border-[#e8e8e8] cursor-pointer hover:border-[#1890ff] hover:shadow-[0_2px_8px_rgba(24,144,255,0.1)] transition-all text-right"
              :class="{ 'opacity-40 cursor-not-allowed': !nextMsg }"
              @click="goToNext"
            >
              <div class="text-[12px] text-[#999]">下一条 →</div>
              <div class="text-[13px] text-[#333] mt-[2px] truncate">{{ nextMsg ? nextMsg.title : '没有更多通知' }}</div>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-else class="bg-white rounded-[8px] p-[60px] shadow-sm text-center">
          <div class="text-[48px] mb-[16px]">📭</div>
          <div class="text-[16px] text-[#999] mb-[16px]">通知不存在或已删除</div>
          <button
            class="h-[35px] px-[18px] bg-[#1890ff] text-white border-none rounded-[4px] text-[13px] cursor-pointer hover:bg-[#40a9ff] transition-colors"
            @click="goBack"
          >返回消息中心</button>
        </div>
      </main>
    </div>
  </div>
</template>
