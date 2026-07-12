<!-- Figma: 164:802 - COU-NOTI-01 - 安全提醒沟通 -->
<template>
  <div class="flex min-h-screen">
    <!-- 侧边栏 -->
    <aside class="w-[232px] min-h-screen flex-shrink-0 text-white flex flex-col" style="background:#00203D;">
      <div class="px-5 py-5 border-b border-white/10 text-lg font-semibold">实习管理系统</div>
      <nav class="flex-1 py-3 px-3 space-y-0.5">
        <router-link to="/counselor/dashboard" class="flex items-center px-4 py-3 rounded-lg text-[16px] text-white/80 hover:bg-white/10">指导工作台</router-link>
        <div>
          <router-link to="/counselor/students" class="flex items-center px-4 py-3 rounded-lg text-[16px]" style="color:#C7D2FE;">我的学生</router-link>
          <div class="ml-6 mt-0.5 space-y-0.5">
            <p class="text-[16px] text-white/80 px-2 py-1">学生列表</p>
            <router-link to="/counselor/students" class="block text-[16px] text-white/80 px-2 py-1 rounded hover:bg-white/10">学生实习跟踪</router-link>
            <router-link to="/counselor/alerts" class="block text-[16px] px-2 py-1 rounded" style="color:#C7D2FE;">安全提醒沟通</router-link>
            <p class="text-[16px] text-white/80 px-2 py-1">家长知情确认</p>
          </div>
        </div>
        <span @click="tip" class="flex items-center px-4 py-3 rounded-lg text-[16px] text-white/80 hover:bg-white/10 cursor-pointer">过程反馈</span>
        <span @click="tip" class="flex items-center px-4 py-3 rounded-lg text-[16px] text-white/80 hover:bg-white/10 cursor-pointer">学生评价</span>
        <!-- TODO: 待确认 — 通知公告页面对应路由，当前暂指向辅导员通知页 -->
        <router-link to="/counselor/notifications" class="flex items-center px-4 py-3 rounded-lg text-[16px] text-white/80 hover:bg-white/10">通知公告</router-link>
      </nav>
    </aside>
    <!-- 右侧主体 -->
    <div class="flex-1 flex flex-col" style="background:#F5F7FB;">
      <!-- 顶部导航栏 -->
      <header class="h-[72px] bg-white flex items-center justify-between px-6 shadow-sm flex-shrink-0">
        <span class="text-[14px]" style="color:#757575;">我的学生 / 安全提醒沟通</span>
        <div class="flex items-center gap-4">
          <div class="flex items-center gap-2">
            <div class="w-10 h-10 rounded-full flex items-center justify-center text-xs font-medium" style="background:#C8C8C8;">孙</div>
            <div>
              <div class="text-[14px] font-medium text-gray-900">孙辅导员</div>
              <div class="text-[12px]" style="color:#2563EB;">辅导员</div>
            </div>
          </div>
          <router-link to="/login" class="text-[14px] font-medium" style="color:#FF383C;">登出</router-link>
        </div>
      </header>
      <!-- 主内容 -->
      <main class="flex-1 overflow-auto px-10 py-6">
        <!-- 页面标题 -->
        <div class="mb-6">
          <p class="text-[14px]" style="color:#757575;">我的学生 / 安全提醒沟通</p>
          <h2 class="text-[32px] font-bold text-gray-900 mt-1">安全提醒沟通</h2>
          <p class="text-[16px] mt-1" style="color:#757575;">向学生和家长发送安全提醒，记录沟通情况</p>
        </div>
        <!-- 上部两列 -->
        <div class="grid grid-cols-[568px_580px] gap-[20px] mb-5">
          <!-- 左侧：发送安全提醒 -->
          <div class="bg-white rounded-[12px] p-6 shadow-sm">
            <h3 class="text-[18px] font-semibold text-gray-900 mb-3">发送安全提醒</h3>
            <!-- 快速模板 -->
            <div class="mb-3">
              <span class="text-[13px] font-medium mr-2" style="color:#374151;">快速模板：</span>
              <span class="text-[12px] cursor-pointer hover:underline mr-3" style="color:#2563EB;">日常安全提醒</span>
              <span class="text-[12px] cursor-pointer hover:underline mr-3" style="color:#2563EB;">节假日安全须知</span>
              <span class="text-[12px] cursor-pointer hover:underline mr-3" style="color:#2563EB;">实习安全提示</span>
              <span class="text-[12px] cursor-pointer hover:underline" style="color:#2563EB;">家长告知书</span>
            </div>
            <!-- 接收人 -->
            <div class="mb-3">
              <span class="text-[13px] font-medium" style="color:#374151;">接收人：</span>
              <span class="text-[13px]" style="color:#2563EB;">{{ selectedStudent.name }} 的家长（{{ selectedStudent.parentPhone }}）</span>
            </div>
            <!-- 提醒内容 -->
            <div class="mb-3">
              <p class="text-[13px] font-medium mb-1" style="color:#374151;">提醒内容：</p>
              <textarea
                v-model="msgContent"
                rows="4"
                class="w-full rounded-[8px] border px-3 py-2 text-[12px] resize-none outline-none"
                style="border-color:rgba(0,0,0,0.16); background:#F9FAFB; color:#374151; height:100px;"
              ></textarea>
            </div>
            <!-- 发送渠道 -->
            <div class="mb-4">
              <span class="text-[13px] font-medium mr-3" style="color:#374151;">发送渠道：</span>
              <label class="inline-flex items-center gap-1 mr-4 cursor-pointer">
                <span class="w-4 h-4 rounded-[4px] flex items-center justify-center" style="background:#2563EB;">
                  <span class="text-[12px] font-bold text-white">&#10003;</span>
                </span>
                <span class="text-[13px]" style="color:#374151;">短信</span>
              </label>
              <label class="inline-flex items-center gap-1 mr-4 cursor-pointer">
                <span class="w-4 h-4 border-[1.5px] rounded-[4px]" style="border-color:rgba(0,0,0,0.25);"></span>
                <span class="text-[13px]" style="color:#374151;">微信</span>
              </label>
              <label class="inline-flex items-center gap-1 cursor-pointer">
                <span class="w-4 h-4 border-[1.5px] rounded-[4px]" style="border-color:rgba(0,0,0,0.25);"></span>
                <span class="text-[13px]" style="color:#374151;">系统消息</span>
              </label>
            </div>
            <!-- 发送按钮 -->
            <button class="h-[44px] w-[120px] rounded-[24px] text-[14px] font-medium text-white hover:opacity-90" style="background:#2563EB;">发送提醒</button>
          </div>
          <!-- 右侧：选择学生 -->
          <div class="bg-white rounded-[12px] p-6 shadow-sm">
            <h3 class="text-[18px] font-semibold text-gray-900 mb-4">选择学生</h3>
            <div class="space-y-3">
              <div
                v-for="s in students"
                :key="s.id"
                class="rounded-[8px] px-4 py-4 cursor-pointer"
                :class="selectedStudent.id === s.id ? 'ring-2 ring-[#2563EB]' : ''"
                style="background:#F9FAFB;"
                @click="selectStudent(s)"
              >
                <div class="flex items-center justify-between">
                  <div>
                    <p class="text-[13px] font-semibold text-gray-900">{{ s.name }}  {{ s.cls }}</p>
                    <p class="text-[12px] mt-0.5" style="color:#6B7280;">家长：{{ s.parentPhone }}</p>
                  </div>
                  <span class="inline-flex items-center rounded-[11px] px-3 py-0.5 text-[11px] font-medium" :style="{background:s.statusBg, color:s.statusColor}">{{ s.status }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
        <!-- 沟通记录 -->
        <div class="bg-white rounded-[12px] p-6 shadow-sm">
          <h3 class="text-[18px] font-semibold text-gray-900 mb-4">沟通记录</h3>
          <!-- 表头 -->
          <div class="flex items-center mb-2">
            <div class="text-[12px] font-semibold" style="color:#6B7280; width:140px;">时间</div>
            <div class="text-[12px] font-semibold" style="color:#6B7280; width:80px;">学生</div>
            <div class="text-[12px] font-semibold" style="color:#6B7280; width:100px;">沟通对象</div>
            <div class="text-[12px] font-semibold" style="color:#6B7280; width:80px;">方式</div>
            <div class="text-[12px] font-semibold" style="color:#6B7280; width:320px;">内容摘要</div>
            <div class="text-[12px] font-semibold" style="color:#6B7280; width:80px;">结果</div>
          </div>
          <!-- 记录行 -->
          <div v-for="(h, i) in history" :key="i">
            <div class="h-px" style="background:#E5E7EB;"></div>
            <div class="flex items-center py-2.5">
              <div class="text-[12px] font-medium" style="color:#6B7280; width:140px;">{{ h.time }}</div>
              <div class="text-[12px] font-medium" style="color:#374151; width:80px;">{{ h.student }}</div>
              <div class="text-[12px] font-medium" style="color:#374151; width:100px;">{{ h.target }}</div>
              <div class="text-[12px] font-medium" style="color:#374151; width:80px;">{{ h.method }}</div>
              <div class="text-[12px]" style="color:#374151; width:320px;">{{ h.summary }}</div>
              <div class="text-[12px] font-medium" style="color:#374151; width:80px;">{{ h.result }}</div>
            </div>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>
<script setup>
import { ref } from 'vue'
function tip() { alert('原型展示，目标待确认') }
const msgContent = ref('各位家长您好：\n近期实习工作进入关键阶段，请提醒学生注意通勤安全，\n严格遵守实习单位的安全管理规定。如有异常请及时联系。\n\n—— 计算机学院辅导员')

const students = [
  { id:1, name:'李明', cls:'软件2201', parentPhone:'138-0000-1234', status:'正常', statusBg:'rgba(22,163,74,0.1)', statusColor:'#16A34A' },
  { id:2, name:'赵强', cls:'计算机2201', parentPhone:'139-0000-5678', status:'异常', statusBg:'rgba(255,56,60,0.1)', statusColor:'#FF383C' },
  { id:3, name:'陈雪', cls:'计算机2202', parentPhone:'137-0000-9012', status:'未提交', statusBg:'rgba(217,119,6,0.1)', statusColor:'#D97706' },
]

const selectedStudent = ref(students[0])
const selectStudent = (s) => { selectedStudent.value = s }

const history = [
  { time:'03-20 14:30', student:'李明', target:'家长(母亲)', method:'电话', summary:'提醒实习通勤安全，确认学生每日打卡', result:'家长已确认' },
  { time:'03-19 10:00', student:'赵强', target:'学生本人', method:'微信', summary:'询问本周未提交周报原因，督促尽快补交', result:'学生承诺补交' },
  { time:'03-18 16:00', student:'陈雪', target:'家长(父亲)', method:'短信', summary:'告知学生实习单位异常情况，建议家校沟通', result:'家长表示关注' },
  { time:'03-15 09:30', student:'王芳', target:'家长(母亲)', method:'电话', summary:'通报学生近期实习表现良好，提醒继续保持', result:'沟通顺利' },
]
</script>
