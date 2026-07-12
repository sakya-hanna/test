<!-- Figma: 164:949 - COU-INTRN-01 - 学生实习跟踪 -->
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
            <router-link to="/counselor/students" class="block text-[16px] px-2 py-1 rounded" style="color:#C7D2FE;">学生实习跟踪</router-link>
            <router-link to="/counselor/alerts" class="block text-[16px] text-white/80 px-2 py-1 rounded hover:bg-white/10">安全提醒沟通</router-link>
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
        <span class="text-[14px]" style="color:#757575;">我的学生 / 学生实习跟踪</span>
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
        <div class="mb-5">
          <p class="text-[14px]" style="color:#757575;">我的学生 / 学生实习跟踪</p>
          <h2 class="text-[32px] font-bold text-gray-900 mt-1">学生实习跟踪</h2>
          <p class="text-[16px] mt-1" style="color:#757575;">按学生查看实习进度详情，及时发现并处理异常情况</p>
        </div>
        <!-- 筛选工具栏 -->
        <div class="flex items-center justify-between mb-5">
          <div class="flex items-center gap-3">
            <span class="text-[14px] font-medium text-gray-900">筛选：</span>
            <span class="text-[14px] font-semibold cursor-pointer" style="color:#2563EB;">全部</span>
            <span class="text-[14px] cursor-pointer" style="color:#6B7280;">正常</span>
            <span class="text-[14px] cursor-pointer" style="color:#6B7280;">异常</span>
            <span class="text-[14px] cursor-pointer" style="color:#6B7280;">未提交周报</span>
          </div>
          <div class="w-[260px]">
            <input type="text" placeholder="搜索学生姓名或学号..." class="w-full h-[36px] rounded-[8px] border px-3 text-[14px] outline-none" style="border-color:rgba(0,0,0,0.16); color:#9CA3AF; background:white;" />
          </div>
        </div>
        <!-- 数据表格 -->
        <div class="bg-white rounded-[8px] shadow-sm mb-4 overflow-hidden">
          <!-- 表头 -->
          <div class="h-[44px] flex items-center px-4" style="background:#F3F4F6;">
            <div class="w-4 mr-5 flex-shrink-0"></div>
            <div class="text-[12px] font-semibold" style="color:#374151; width:80px;">学生姓名</div>
            <div class="text-[12px] font-semibold" style="color:#374151; width:100px;">学号</div>
            <div class="text-[12px] font-semibold" style="color:#374151; width:100px;">班级</div>
            <div class="text-[12px] font-semibold" style="color:#374151; width:160px;">实习企业</div>
            <div class="text-[12px] font-semibold" style="color:#374151; width:120px;">实习类型</div>
            <div class="text-[12px] font-semibold" style="color:#374151; width:120px;">周报进度</div>
            <div class="text-[12px] font-semibold" style="color:#374151; width:120px;">最近提交</div>
            <div class="text-[12px] font-semibold" style="color:#374151; width:80px;">状态</div>
            <div class="text-[12px] font-semibold" style="color:#374151; width:60px;">操作</div>
          </div>
          <!-- 数据行 -->
          <div v-for="(r, i) in rows" :key="r.id">
            <div class="h-[52px] flex items-center px-4">
              <div class="w-4 h-4 border-[1.5px] rounded-[4px] mr-5 flex-shrink-0" style="border-color:rgba(0,0,0,0.25);"></div>
              <div class="text-[12px]" style="color:#374151; width:80px;">{{ r.name }}</div>
              <div class="text-[12px]" style="color:#374151; width:100px;">{{ r.sid }}</div>
              <div class="text-[12px]" style="color:#374151; width:100px;">{{ r.cls }}</div>
              <div class="text-[12px]" style="color:#374151; width:160px;">{{ r.company }}</div>
              <div class="text-[12px]" style="color:#374151; width:120px;">{{ r.type }}</div>
              <div class="text-[12px]" style="color:#374151; width:120px;">{{ r.progress }}</div>
              <div class="text-[12px]" style="color:#374151; width:120px;">{{ r.lastSubmit }}</div>
              <div style="width:80px;">
                <span class="inline-flex items-center rounded-[12px] px-3 py-1 text-[11px] font-medium" :style="{background:r.statusBg, color:r.statusColor}">{{ r.status }}</span>
              </div>
              <div style="width:60px;">
                <span @click="showDetail(r)" class="text-[13px] font-medium cursor-pointer hover:underline" :style="{color:r.actionColor}">{{ r.action }}</span>
              </div>
            </div>
            <div v-if="i < rows.length - 1" class="h-px mx-4" style="background:#E5E7EB;"></div>
          </div>
        </div>
        <!-- 分页 -->
        <div class="h-[44px] bg-white rounded-[8px] flex items-center px-4">
          <span class="text-[13px]" style="color:#6B7280;">共 156 名学生</span>
        </div>
      </main>
    </div>
  </div>
</template>
<script setup>
function tip() { alert('原型展示，目标待确认') }
function showDetail(r) { alert(`学生详情（演示）\n${r.name} — ${r.company}\n${r.type} · ${r.progress} · ${r.status}`) }
const rows = [
  { id:1, name:'李明', sid:'2024030112', cls:'软件2201', company:'无锡科技有限公司', type:'毕业实习', progress:'3/12', lastSubmit:'03-20 周报', status:'正常', statusBg:'rgba(22,163,74,0.1)', statusColor:'#16A34A', action:'详情', actionColor:'#2563EB' },
  { id:2, name:'王芳', sid:'2024030115', cls:'软件2202', company:'苏州创新技术公司', type:'企业需求对接', progress:'2/12', lastSubmit:'03-18 月报', status:'正常', statusBg:'rgba(22,163,74,0.1)', statusColor:'#16A34A', action:'详情', actionColor:'#2563EB' },
  { id:3, name:'赵强', sid:'2024030120', cls:'计算机2201', company:'南京软件有限公司', type:'毕业实习', progress:'1/12', lastSubmit:'03-10 周报', status:'异常', statusBg:'rgba(255,56,60,0.1)', statusColor:'#FF383C', action:'详情', actionColor:'#2563EB' },
  { id:4, name:'陈雪', sid:'2024030125', cls:'计算机2202', company:'上海数据集团', type:'灵活实习', progress:'0/12', lastSubmit:'未提交', status:'未提交', statusBg:'rgba(217,119,6,0.1)', statusColor:'#D97706', action:'提醒', actionColor:'#FF383C' },
  { id:5, name:'刘洋', sid:'2024030130', cls:'软件2201', company:'杭州云科技有限公司', type:'认知实习', progress:'4/4', lastSubmit:'03-15 报告', status:'已完成', statusBg:'rgba(107,114,128,0.1)', statusColor:'#6B7280', action:'详情', actionColor:'#2563EB' },
  { id:6, name:'周婷', sid:'2024030135', cls:'计算机2201', company:'常州智能装备公司', type:'毕业实习', progress:'3/12', lastSubmit:'03-17 周报', status:'正常', statusBg:'rgba(22,163,74,0.1)', statusColor:'#16A34A', action:'详情', actionColor:'#2563EB' },
]
</script>
