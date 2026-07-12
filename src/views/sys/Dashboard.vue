<!-- SYS-DASH-01 系统管理员工作台 -->
<template>
  <div class="flex min-h-screen">
    <aside class="w-[232px] min-h-screen flex-shrink-0 text-white flex flex-col" style="background:#00203D;">
      <div class="px-5 py-5 border-b border-white/10 text-lg font-semibold">实习管理系统</div>
      <nav class="flex-1 py-3 px-3 space-y-1">
        <router-link to="/admin/dashboard" class="flex items-center px-4 py-3 rounded-lg text-[16px]" style="color:#C7D2FE;">工作台</router-link>
        <router-link to="/admin/users" class="flex items-center px-4 py-3 rounded-lg text-[16px] text-white/80 hover:bg-white/10">用户管理</router-link>
        <router-link to="/admin/roles" class="flex items-center px-4 py-3 rounded-lg text-[16px] text-white/80 hover:bg-white/10">权限配置</router-link>
        <router-link to="/admin/settings" class="flex items-center px-4 py-3 rounded-lg text-[16px] text-white/80 hover:bg-white/10">全局配置</router-link>
        <router-link to="/admin/logs" class="flex items-center px-4 py-3 rounded-lg text-[16px] text-white/80 hover:bg-white/10">系统日志</router-link>
        <router-link to="/admin/errors" class="flex items-center px-4 py-3 rounded-lg text-[16px] text-white/80 hover:bg-white/10">异常处理</router-link>
      </nav>
    </aside>
    <div class="flex-1 flex flex-col" style="background:#F5F7FB;">
      <header class="h-[72px] bg-white flex items-center justify-between px-6 shadow-sm flex-shrink-0">
        <span class="text-[14px]" style="color:#757575;">系统管理 / 工作台</span>
        <router-link to="/login" class="text-[14px] font-medium" style="color:#FF383C;">登出</router-link>
      </header>
      <main class="flex-1 overflow-auto p-6">
        <h2 class="text-[32px] font-bold text-gray-900 mb-1">系统管理员工作台</h2>
        <p class="text-[16px] mb-6" style="color:#757575;">系统全局管理与监控</p>
        <div class="grid grid-cols-4 gap-4">
          <router-link v-for="l in links" :key="l.k" :to="'/admin/'+l.k" class="bg-white rounded-2xl p-6 shadow-sm text-center hover:shadow-md">
            <h4 class="font-semibold text-gray-900">{{ l.n }}</h4>
            <p class="text-xs mt-1" style="color:#6B7280;">{{ l.d }}</p>
          </router-link>
        </div>
      </main>
    </div>
  </div>
</template>
<script setup>
const links = [
  { k: 'users', n: '用户管理', d: '管理系统用户账号' },
  { k: 'roles', n: '权限配置', d: '角色与权限设置' },
  { k: 'settings', n: '全局配置', d: '系统参数与配置' },
  { k: 'logs', n: '系统日志', d: '操作日志与审计' },
]
</script>
