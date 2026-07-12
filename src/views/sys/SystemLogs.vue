<!-- SYS-LOG-01 系统日志 -->
<template>
  <div class="flex min-h-screen">
    <aside class="w-[232px] min-h-screen flex-shrink-0 text-white flex flex-col" style="background:#00203D;">
      <div class="px-5 py-5 border-b border-white/10 text-lg font-semibold">实习管理系统</div>
      <nav class="flex-1 py-3 px-3 space-y-1">
        <router-link to="/admin/dashboard" class="flex items-center px-4 py-3 rounded-lg text-[16px] text-white/80 hover:bg-white/10">工作台</router-link>
        <router-link to="/admin/users" class="flex items-center px-4 py-3 rounded-lg text-[16px] text-white/80 hover:bg-white/10">用户管理</router-link>
        <router-link to="/admin/roles" class="flex items-center px-4 py-3 rounded-lg text-[16px] text-white/80 hover:bg-white/10">权限配置</router-link>
        <router-link to="/admin/settings" class="flex items-center px-4 py-3 rounded-lg text-[16px] text-white/80 hover:bg-white/10">全局配置</router-link>
        <router-link to="/admin/logs" class="flex items-center px-4 py-3 rounded-lg text-[16px]" style="color:#C7D2FE;">系统日志</router-link>
        <router-link to="/admin/errors" class="flex items-center px-4 py-3 rounded-lg text-[16px] text-white/80 hover:bg-white/10">异常处理</router-link>
      </nav>
    </aside>
    <div class="flex-1 flex flex-col" style="background:#F5F7FB;">
      <header class="h-[72px] bg-white flex items-center justify-between px-6 shadow-sm flex-shrink-0">
        <span class="text-[14px]" style="color:#757575;">系统管理 / 系统日志</span>
        <router-link to="/login" class="text-[14px] font-medium" style="color:#FF383C;">登出</router-link>
      </header>
      <main class="flex-1 overflow-auto p-6">
        <h2 class="text-[32px] font-bold text-gray-900 mb-1">系统日志</h2>
        <p class="text-[16px] mb-6" style="color:#757575;">查看系统操作日志与审计记录</p>

        <div class="flex items-center gap-4 mb-4">
          <select v-model="typeFilter" class="px-4 py-2 rounded-[8px] border border-[rgba(0,0,0,0.16)] text-[14px] bg-white">
            <option value="">全部类型</option><option>登录</option><option>操作</option><option>异常</option>
          </select>
          <span class="text-[13px] text-gray-400">原型展示 — 日志数据为静态演示</span>
        </div>

        <div class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] overflow-hidden">
          <table class="w-full text-[14px]">
            <thead><tr style="background:#F5F7FB;">
              <th class="text-left px-4 py-3 font-medium text-[#6B7280]">时间</th>
              <th class="text-left px-4 py-3 font-medium text-[#6B7280]">类型</th>
              <th class="text-left px-4 py-3 font-medium text-[#6B7280]">用户</th>
              <th class="text-left px-4 py-3 font-medium text-[#6B7280]">IP 地址</th>
              <th class="text-left px-4 py-3 font-medium text-[#6B7280]">操作描述</th>
              <th class="text-left px-4 py-3 font-medium text-[#6B7280]">结果</th>
            </tr></thead>
            <tbody>
              <tr v-for="l in filteredLogs" :key="l.id" class="border-b border-gray-100">
                <td class="px-4 py-3 text-gray-500 text-[13px]">{{ l.time }}</td>
                <td class="px-4 py-3"><span class="px-2 py-1 rounded-full text-xs font-medium"
                  :class="l.type==='登录'?'text-blue-600 bg-blue-50':l.type==='操作'?'text-gray-600 bg-gray-100':'text-red-600 bg-red-50'">{{ l.type }}</span></td>
                <td class="px-4 py-3 text-gray-700">{{ l.user }}</td>
                <td class="px-4 py-3 text-gray-500 text-[13px]">{{ l.ip }}</td>
                <td class="px-4 py-3 text-gray-600">{{ l.detail }}</td>
                <td class="px-4 py-3"><span :style="{color:l.ok?'#16A34A':'#FF383C'}">{{ l.ok?'成功':'失败' }}</span></td>
              </tr>
            </tbody>
          </table>
        </div>
      </main>
    </div>
  </div>
</template>
<script setup>
import { ref, computed } from 'vue'
const typeFilter = ref('')
const logs = ref([
  { id:1,time:'2026-07-05 08:30:12',type:'登录',user:'张院长',ip:'192.168.1.100',detail:'学院管理员登录系统',ok:true },
  { id:2,time:'2026-07-05 08:25:03',type:'操作',user:'王处长',ip:'192.168.1.50',detail:'审核通过企业入驻申请 #6',ok:true },
  { id:3,time:'2026-07-05 08:20:47',type:'登录',user:'SA赵工',ip:'10.0.0.1',detail:'系统管理员登录后台',ok:true },
  { id:4,time:'2026-07-05 08:18:22',type:'异常',user:'—',ip:'45.33.32.156',detail:'连续登录失败 5 次，账号已锁定',ok:false },
  { id:5,time:'2026-07-05 07:55:10',type:'操作',user:'李老师',ip:'192.168.2.20',detail:'批阅学生张同学周报',ok:true },
  { id:6,time:'2026-07-05 07:40:01',type:'登录',user:'李老师',ip:'192.168.2.20',detail:'教师登录系统',ok:true },
])
const filteredLogs = computed(() => typeFilter.value ? logs.value.filter(l => l.type === typeFilter.value) : logs.value)
</script>
