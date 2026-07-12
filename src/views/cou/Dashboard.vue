<!-- Figma: 164:1102 - COU-DASH-01 - 辅导员工作台 -->
<template>
  <div class="flex min-h-screen">
    <!-- 侧边栏 -->
    <aside class="w-[232px] min-h-screen flex-shrink-0 text-white flex flex-col" style="background:#00203D;">
      <div class="px-5 py-5 border-b border-white/10 text-lg font-semibold">实习管理系统</div>
      <nav class="flex-1 py-3 px-3 space-y-1">
        <router-link to="/counselor/dashboard" class="flex items-center px-4 py-3 rounded-lg text-[16px]" style="color:#C7D2FE;">指导工作台</router-link>
        <router-link to="/counselor/students" class="flex items-center px-4 py-3 rounded-lg text-[16px] text-white/80 hover:bg-white/10">我的学生</router-link>
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
        <div class="flex items-center gap-6">
          <span class="text-[14px]" style="color:#757575;">首页 / 指导工作台</span>
        </div>
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
          <p class="text-[14px]" style="color:#757575;">首页 / 指导工作台</p>
          <h2 class="text-[32px] font-bold text-gray-900 mt-1">辅导员工作台</h2>
          <p class="text-[16px] mt-1" style="color:#757575;">实时掌握所带学生实习动态，快速响应异常情况</p>
        </div>
        <!-- 统计卡片 -->
        <div class="grid grid-cols-4 gap-[22px] mb-6">
          <div class="bg-white rounded-[12px] p-5 shadow-sm">
            <p class="text-[13px] mb-2" style="color:#6B7280;">所带学生</p>
            <p class="text-[28px] font-bold" style="color:#2563EB;">156 人</p>
          </div>
          <div class="bg-white rounded-[12px] p-5 shadow-sm">
            <p class="text-[13px] mb-2" style="color:#6B7280;">实习中</p>
            <p class="text-[28px] font-bold" style="color:#16A34A;">128 人</p>
          </div>
          <div class="bg-white rounded-[12px] p-5 shadow-sm">
            <p class="text-[13px] mb-2" style="color:#6B7280;">异常预警</p>
            <p class="text-[28px] font-bold" style="color:#FF383C;">8 人</p>
          </div>
          <div class="bg-white rounded-[12px] p-5 shadow-sm">
            <p class="text-[13px] mb-2" style="color:#6B7280;">待沟通</p>
            <p class="text-[28px] font-bold" style="color:#D97706;">3 人</p>
          </div>
        </div>
        <!-- 两列上部 -->
        <div class="grid grid-cols-[568px_580px] gap-[20px] mb-6">
          <!-- 异常预警 -->
          <div class="bg-white rounded-[12px] p-6 shadow-sm">
            <h3 class="text-[18px] font-semibold text-gray-900 mb-1">异常预警</h3>
            <p class="text-[12px] mb-4" style="color:#9CA3AF;">以下学生需要关注</p>
            <div class="space-y-3">
              <div v-for="a in alerts" :key="a.id" class="flex items-center rounded-[8px] px-4 py-3" style="background:#F9FAFB;">
                <div class="flex-1">
                  <p class="text-[13px] font-semibold text-gray-900">{{ a.name }}</p>
                  <p class="text-[12px] mt-0.5" style="color:#6B7280;">{{ a.reason }}</p>
                </div>
                <span class="inline-flex items-center rounded-[12px] px-3 py-1 text-[11px] font-medium" :style="{background:a.lvBg, color:a.lvColor}">{{ a.lv }}</span>
                <span class="text-[12px] font-medium ml-4 cursor-pointer" style="color:#2563EB;">联系学生</span>
              </div>
            </div>
          </div>
          <!-- 学生实习概览 -->
          <div class="bg-white rounded-[12px] p-6 shadow-sm">
            <h3 class="text-[18px] font-semibold text-gray-900 mb-4">学生实习概览</h3>
            <div class="space-y-3">
              <div v-for="o in ovs" :key="o.t" class="flex items-center rounded-[8px] px-4 py-[11px]" style="background:#F9FAFB;">
                <span class="text-[14px] font-medium text-gray-900 flex-1">{{ o.t }}</span>
                <span class="text-[14px] font-semibold mr-3" :style="{color:o.c}">{{ o.n }}人</span>
                <span class="inline-flex items-center rounded-[12px] px-3 py-1 text-[11px] font-medium" :style="{background:o.sBg, color:o.c}">{{ o.s }}</span>
              </div>
            </div>
          </div>
        </div>
        <!-- 两列下部 -->
        <div class="grid grid-cols-[568px_580px] gap-[20px]">
          <!-- 最近沟通记录 -->
          <div class="bg-white rounded-[12px] p-6 shadow-sm">
            <h3 class="text-[18px] font-semibold text-gray-900 mb-4">最近沟通记录</h3>
            <div class="space-y-3">
              <div v-for="c in comms" :key="c.id" class="rounded-[8px] px-4 py-3" style="background:#F9FAFB;">
                <p class="text-[13px] font-medium text-gray-900">{{ c.name }}  {{ c.date }}  {{ c.m }}</p>
                <p class="text-[12px] mt-1" style="color:#6B7280;">{{ c.note }}</p>
              </div>
            </div>
          </div>
          <!-- 快捷操作 -->
          <div class="bg-white rounded-[12px] p-6 shadow-sm">
            <h3 class="text-[18px] font-semibold text-gray-900 mb-4">快捷操作</h3>
            <div class="grid grid-cols-4 gap-3">
              <div @click="quickAction('批量发送提醒')" class="flex items-center justify-center rounded-[12px] py-5 px-3 cursor-pointer hover:opacity-80" style="background:#F9FAFB;">
                <span class="text-[13px] font-medium text-center" style="color:#2563EB;">批量发送提醒</span>
              </div>
              <div @click="quickAction('导出学生列表')" class="flex items-center justify-center rounded-[12px] py-5 px-3 cursor-pointer hover:opacity-80" style="background:#F9FAFB;">
                <span class="text-[13px] font-medium text-center" style="color:#2563EB;">导出学生列表</span>
              </div>
              <div @click="quickAction('生成实习报告')" class="flex items-center justify-center rounded-[12px] py-5 px-3 cursor-pointer hover:opacity-80" style="background:#F9FAFB;">
                <span class="text-[13px] font-medium text-center" style="color:#2563EB;">生成实习报告</span>
              </div>
              <div class="flex items-center justify-center rounded-[12px] py-5 px-3 cursor-pointer hover:opacity-80" style="background:#F9FAFB;">
                <span class="text-[13px] font-medium text-center" style="color:#2563EB;">联系家长</span>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>
<script setup>
function tip() { alert('原型展示，目标待确认') }
function quickAction(name) { alert(`原型展示 — ${name}（待后端接入）`) }
const alerts = [
  { id:1, name:'李明  软件2201', reason:'连续2周未提交周报', lv:'紧急', lvBg:'rgba(255,56,60,0.15)', lvColor:'#FF383C' },
  { id:2, name:'王芳  软件2202', reason:'实习企业变更未报备', lv:'重要', lvBg:'rgba(217,119,6,0.15)', lvColor:'#D97706' },
  { id:3, name:'赵强  计算机2201', reason:'本周日报未提交', lv:'提醒', lvBg:'rgba(37,99,235,0.15)', lvColor:'#2563EB' },
  { id:4, name:'陈雪  计算机2202', reason:'实习单位反馈异常', lv:'重要', lvBg:'rgba(217,119,6,0.15)', lvColor:'#D97706' },
]
const ovs = [
  { t:'毕业实习', n:86, s:'进行中', c:'#2563EB', sBg:'rgba(37,99,235,0.1)' },
  { t:'企业需求对接', n:24, s:'进行中', c:'#16A34A', sBg:'rgba(22,163,74,0.1)' },
  { t:'灵活实习', n:12, s:'进行中', c:'#D97706', sBg:'rgba(217,119,6,0.1)' },
  { t:'认知实习', n:8, s:'已完成', c:'#6B7280', sBg:'rgba(107,114,128,0.1)' },
  { t:'暑期社会实践', n:16, s:'未开始', c:'#9CA3AF', sBg:'rgba(156,163,175,0.1)' },
]
const comms = [
  { id:1, name:'李明', date:'2026-03-20', m:'电话沟通', note:'提醒周报提交' },
  { id:2, name:'王芳', date:'2026-03-19', m:'微信沟通', note:'确认企业变更情况' },
  { id:3, name:'赵强', date:'2026-03-18', m:'家长沟通', note:'反馈学生实习情况' },
]
</script>
