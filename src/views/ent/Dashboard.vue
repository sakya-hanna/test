<!-- Figma: 164:659 - ENT-DASH-02 - 企业工作台 1440x1024 -->
<template>
  <div class="flex min-h-screen">
    <!-- 侧边栏 232x1024 bg=#00203D -->
    <CompanySidebar />

    <!-- 右侧内容 bg=#F5F7FB -->
    <div class="flex-1 flex flex-col" style="background:#F5F7FB;">
      <!-- 顶栏 h=72px -->
      <header class="h-[72px] bg-white flex items-center justify-between px-6 shadow-sm flex-shrink-0">
        <span class="text-[14px]" style="color:#757575;">首页 / 企业工作台</span>
        <div class="flex items-center gap-4">
          <div class="flex items-center gap-2">
            <div class="w-10 h-10 rounded-full flex items-center justify-center text-xs font-medium" style="background:#C8C8C8;">王</div>
            <div>
              <div class="text-[14px] font-medium text-gray-900">王经理</div>
              <div class="text-[12px]" style="color:#16A34A;">企业用户</div>
            </div>
          </div>
          <router-link to="/login" class="text-[14px] font-medium" style="color:#FF383C;">登出</router-link>
        </div>
      </header>

      <!-- 主内容 p=24px -->
      <main class="flex-1 overflow-auto p-6">
        <!-- 标题区 -->
        <div class="mb-6">
          <p class="text-[14px]" style="color:#757575;">首页 / 企业工作台</p>
          <h2 class="text-[32px] font-bold text-gray-900 mt-1">企业工作台</h2>
          <p class="text-[16px] mt-1" style="color:#757575;">欢迎回来，无锡科技有限公司</p>
        </div>

        <!-- 4 统计卡片 -->
        <div class="grid grid-cols-4 gap-5 mb-6">
          <div class="bg-white rounded-2xl p-5 shadow-sm">
            <p class="text-[13px] mb-2" style="color:#6B7280;">发布岗位</p>
            <p class="text-[28px] font-bold" style="color:#2563EB;">8 个</p>
          </div>
          <div class="bg-white rounded-2xl p-5 shadow-sm">
            <p class="text-[13px] mb-2" style="color:#6B7280;">收到申请</p>
            <p class="text-[28px] font-bold" style="color:#16A34A;">45 份</p>
          </div>
          <div class="bg-white rounded-2xl p-5 shadow-sm">
            <p class="text-[13px] mb-2" style="color:#6B7280;">合作中</p>
            <p class="text-[28px] font-bold" style="color:#2563EB;">3 项</p>
          </div>
          <div class="bg-white rounded-2xl p-5 shadow-sm">
            <p class="text-[13px] mb-2" style="color:#6B7280;">待处理</p>
            <p class="text-[28px] font-bold" style="color:#D97706;">12 项</p>
          </div>
        </div>

        <!-- 双列上 -->
        <div class="grid grid-cols-2 gap-6 mb-6">
          <!-- 最近收到的申请 -->
          <div class="bg-white rounded-2xl p-6 shadow-sm">
            <h3 class="text-[18px] font-semibold text-gray-900 mb-4">最近收到的申请</h3>
            <div class="space-y-3">
              <div v-for="a in apps" :key="a.id" class="flex items-center justify-between rounded-lg px-4 py-3" style="background:#F9FAFB;">
                <div class="flex-1">
                  <p class="text-[13px] font-semibold text-gray-900">{{ a.name }}</p>
                  <p class="text-[12px] mt-0.5" style="color:#6B7280;">{{ a.job }}  |  {{ a.date }}</p>
                </div>
                <span class="inline-flex items-center rounded-md px-3 py-1 text-[11px] font-medium"
                  :style="a.st==='待处理'?'background:#2563EB; color:#fff;':'background:#F3F4F6; color:#6B7280;'">{{ a.st }}</span>
                <span @click="handleApp(a)" class="text-[12px] font-medium ml-3 cursor-pointer hover:underline" style="color:#2563EB;">{{ a.st==='待处理'?'处理':'查看' }}</span>
              </div>
            </div>
          </div>

          <!-- 在招岗位 -->
          <div class="bg-white rounded-2xl p-6 shadow-sm">
            <h3 class="text-[18px] font-semibold text-gray-900 mb-4">在招岗位</h3>
            <div class="space-y-3">
              <div v-for="j in jobs" :key="j.id" class="flex items-center rounded-lg px-4 py-3" style="background:#F9FAFB;">
                <div class="flex-1"><p class="text-[14px] font-semibold text-gray-900">{{ j.name }}</p><p class="text-[12px] mt-0.5" style="color:#6B7280;">需求{{ j.n }}人</p></div>
                <div class="flex-1 flex justify-center"><span class="inline-flex items-center rounded-md px-3 py-1 text-[11px] font-medium" :class="j.st==='招聘中'?'text-[#16A34A]':'text-[#FF383C]'">{{ j.st }}</span></div>
                <span @click="editJob(j)" class="text-[12px] font-medium cursor-pointer flex-shrink-0 text-center hover:underline" style="width:40px; color:#2563EB;">编辑</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 双列下 -->
        <div class="grid grid-cols-2 gap-6">
          <!-- 合作项目 -->
          <div class="bg-white rounded-2xl p-6 shadow-sm">
            <h3 class="text-[18px] font-semibold text-gray-900 mb-4">合作项目</h3>
            <div class="space-y-3">
              <div v-for="p in projs" :key="p.id" class="rounded-lg px-4 py-3" style="background:#F9FAFB;">
                <p class="text-[14px] font-semibold text-gray-900">{{ p.name }}</p>
                <p class="text-[12px] mt-0.5" :style="{color:p.c}">{{ p.d }}</p>
              </div>
            </div>
          </div>
          <!-- 快捷操作 -->
          <div class="bg-white rounded-2xl p-6 shadow-sm">
            <h3 class="text-[18px] font-semibold text-gray-900 mb-4">快捷操作</h3>
            <div class="grid grid-cols-2 gap-3">
              <router-link to="/company/jobs" class="flex items-center justify-center rounded-xl py-5 px-3 hover:opacity-80" style="background:#F9FAFB;"><span class="text-[12px] font-medium" style="color:#2563EB;">发布新岗位</span></router-link>
              <router-link to="/company/applications" class="flex items-center justify-center rounded-xl py-5 px-3 hover:opacity-80" style="background:#F9FAFB;"><span class="text-[12px] font-medium" style="color:#2563EB;">查看申请</span></router-link>
              <router-link to="/company/profile" class="flex items-center justify-center rounded-xl py-5 px-3 hover:opacity-80" style="background:#F9FAFB;"><span class="text-[12px] font-medium" style="color:#2563EB;">企业信息维护</span></router-link>
              <div class="flex items-center justify-center rounded-xl py-5 px-3 cursor-pointer hover:opacity-80" style="background:#F9FAFB;"><span class="text-[12px] font-medium" style="color:#2563EB;">联系校方</span></div>
            </div>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import CompanySidebar from '../../components/CompanySidebar.vue'
const apps=[{id:1,name:'李明  软件工程',job:'Java开发工程师',date:'2026-03-20',st:'待处理'},{id:2,name:'王芳  计算机科学',job:'前端开发实习生',date:'2026-03-19',st:'待处理'},{id:3,name:'赵强  软件工程',job:'测试工程师',date:'2026-03-18',st:'已处理'},{id:4,name:'陈雪  数据科学',job:'数据分析实习生',date:'2026-03-17',st:'已处理'}]
const jobs=[{id:1,name:'Java开发工程师',n:3,st:'招聘中'},{id:2,name:'前端开发实习生',n:2,st:'招聘中'},{id:3,name:'测试工程师',n:1,st:'已招满'},{id:4,name:'数据分析实习生',n:2,st:'招聘中'}]
const projs=[{id:1,name:'2026春季毕业实习',d:'计算机学院 · 进行中',c:'#2563EB'},{id:2,name:'企业需求对接项目',d:'软件学院 · 进行中',c:'#16A34A'},{id:3,name:'认知实习合作',d:'电子学院 · 已完成',c:'#6B7280'}]
function handleApp(a) { alert(`学生申请（演示）：${a.name} — ${a.job}`); a.st = '已处理' }
function editJob(j) { alert(`编辑岗位（演示）：${j.name}`) }
</script>
