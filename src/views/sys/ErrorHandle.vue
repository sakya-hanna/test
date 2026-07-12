<!-- SYS-LOG-02 异常处理 -->
<template>
  <div class="flex min-h-screen">
    <aside class="w-[232px] min-h-screen flex-shrink-0 text-white flex flex-col" style="background:#00203D;">
      <div class="px-5 py-5 border-b border-white/10 text-lg font-semibold">实习管理系统</div>
      <nav class="flex-1 py-3 px-3 space-y-1">
        <router-link to="/admin/dashboard" class="flex items-center px-4 py-3 rounded-lg text-[16px] text-white/80 hover:bg-white/10">工作台</router-link>
        <router-link to="/admin/users" class="flex items-center px-4 py-3 rounded-lg text-[16px] text-white/80 hover:bg-white/10">用户管理</router-link>
        <router-link to="/admin/roles" class="flex items-center px-4 py-3 rounded-lg text-[16px] text-white/80 hover:bg-white/10">权限配置</router-link>
        <router-link to="/admin/settings" class="flex items-center px-4 py-3 rounded-lg text-[16px] text-white/80 hover:bg-white/10">全局配置</router-link>
        <router-link to="/admin/logs" class="flex items-center px-4 py-3 rounded-lg text-[16px] text-white/80 hover:bg-white/10">系统日志</router-link>
        <router-link to="/admin/errors" class="flex items-center px-4 py-3 rounded-lg text-[16px]" style="color:#C7D2FE;">异常处理</router-link>
      </nav>
    </aside>
    <div class="flex-1 flex flex-col" style="background:#F5F7FB;">
      <header class="h-[72px] bg-white flex items-center justify-between px-6 shadow-sm flex-shrink-0">
        <span class="text-[14px]" style="color:#757575;">系统管理 / 异常处理</span>
        <router-link to="/login" class="text-[14px] font-medium" style="color:#FF383C;">登出</router-link>
      </header>
      <main class="flex-1 overflow-auto p-6">
        <h2 class="text-[32px] font-bold text-gray-900 mb-1">异常处理</h2>
        <p class="text-[16px] mb-6" style="color:#757575;">查看与处理系统异常记录</p>

        <div class="flex items-center gap-4 mb-4">
          <select v-model="levelFilter" class="px-4 py-2 rounded-[8px] border border-[rgba(0,0,0,0.16)] text-[14px] bg-white">
            <option value="">全部级别</option><option>严重</option><option>警告</option><option>提示</option>
          </select>
          <span class="text-[13px] text-gray-400">原型展示 — 异常数据为静态演示</span>
        </div>

        <div class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] overflow-hidden">
          <table class="w-full text-[14px]">
            <thead><tr style="background:#F5F7FB;">
              <th class="text-left px-4 py-3 font-medium text-[#6B7280]">时间</th>
              <th class="text-left px-4 py-3 font-medium text-[#6B7280]">级别</th>
              <th class="text-left px-4 py-3 font-medium text-[#6B7280]">来源</th>
              <th class="text-left px-4 py-3 font-medium text-[#6B7280]">异常描述</th>
              <th class="text-left px-4 py-3 font-medium text-[#6B7280]">状态</th>
              <th class="text-left px-4 py-3 font-medium text-[#6B7280]">操作</th>
            </tr></thead>
            <tbody>
              <tr v-for="e in filteredErrors" :key="e.id" class="border-b border-gray-100">
                <td class="px-4 py-3 text-gray-500 text-[13px]">{{ e.time }}</td>
                <td class="px-4 py-3"><span class="px-2 py-1 rounded-full text-xs font-medium"
                  :class="e.level==='严重'?'text-white bg-[#FF383C]':e.level==='警告'?'text-[#D97706] bg-orange-50':'text-gray-600 bg-gray-100'">{{ e.level }}</span></td>
                <td class="px-4 py-3 text-gray-700">{{ e.source }}</td>
                <td class="px-4 py-3 text-gray-600">{{ e.desc }}</td>
                <td class="px-4 py-3"><span :style="{color:e.done?'#16A34A':'#F59E0B'}">{{ e.done?'已处理':'待处理' }}</span></td>
                <td class="px-4 py-3"><button v-if="!e.done" @click="handle(e)" class="text-[#2563EB] text-[14px] cursor-pointer hover:underline">处理</button><span v-else class="text-[14px] text-gray-400">—</span></td>
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
const levelFilter = ref('')
const errors = ref([
  { id:1,time:'2026-07-05 08:18:22',level:'严重',source:'认证模块',desc:'IP 45.33.32.156 连续登录失败触发锁定',done:false },
  { id:2,time:'2026-07-05 07:30:10',level:'警告',source:'文件服务',desc:'文件上传接口超时（3.2s），超过阈值',done:false },
  { id:3,time:'2026-07-04 22:15:00',level:'严重',source:'数据库',desc:'数据库连接池耗尽，已自动恢复',done:true },
  { id:4,time:'2026-07-04 18:00:05',level:'提示',source:'通知服务',desc:'短信网关返回延迟，已重试成功',done:true },
  { id:5,time:'2026-07-04 14:20:33',level:'警告',source:'认证模块',desc:'SSO 认证回调超时，用户重新登录',done:true },
])
const filteredErrors = computed(() => levelFilter.value ? errors.value.filter(e => e.level === levelFilter.value) : errors.value)
function handle(e) { e.done = true; alert(`异常 #${e.id} 已标记为已处理（演示）`) }
</script>
