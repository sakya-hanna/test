<!-- SYS-CFG-02 权限配置 -->
<template>
  <div class="flex min-h-screen">
    <aside class="w-[232px] min-h-screen flex-shrink-0 text-white flex flex-col" style="background:#00203D;">
      <div class="px-5 py-5 border-b border-white/10 text-lg font-semibold">实习管理系统</div>
      <nav class="flex-1 py-3 px-3 space-y-1">
        <router-link to="/admin/dashboard" class="flex items-center px-4 py-3 rounded-lg text-[16px] text-white/80 hover:bg-white/10">工作台</router-link>
        <router-link to="/admin/users" class="flex items-center px-4 py-3 rounded-lg text-[16px] text-white/80 hover:bg-white/10">用户管理</router-link>
        <router-link to="/admin/roles" class="flex items-center px-4 py-3 rounded-lg text-[16px]" style="color:#C7D2FE;">权限配置</router-link>
        <router-link to="/admin/settings" class="flex items-center px-4 py-3 rounded-lg text-[16px] text-white/80 hover:bg-white/10">全局配置</router-link>
        <router-link to="/admin/logs" class="flex items-center px-4 py-3 rounded-lg text-[16px] text-white/80 hover:bg-white/10">系统日志</router-link>
        <router-link to="/admin/errors" class="flex items-center px-4 py-3 rounded-lg text-[16px] text-white/80 hover:bg-white/10">异常处理</router-link>
      </nav>
    </aside>
    <div class="flex-1 flex flex-col" style="background:#F5F7FB;">
      <header class="h-[72px] bg-white flex items-center justify-between px-6 shadow-sm flex-shrink-0">
        <span class="text-[14px]" style="color:#757575;">系统管理 / 权限配置</span>
        <router-link to="/login" class="text-[14px] font-medium" style="color:#FF383C;">登出</router-link>
      </header>
      <main class="flex-1 overflow-auto p-6">
        <h2 class="text-[32px] font-bold text-gray-900 mb-1">权限配置</h2>
        <p class="text-[16px] mb-6" style="color:#757575;">管理系统角色与操作权限</p>

        <div class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] overflow-hidden">
          <table class="w-full text-[14px]">
            <thead><tr style="background:#F5F7FB;">
              <th class="text-left px-4 py-3 font-medium text-[#6B7280]">角色</th>
              <th class="text-center px-4 py-3 font-medium text-[#6B7280]">查看</th>
              <th class="text-center px-4 py-3 font-medium text-[#6B7280]">新增</th>
              <th class="text-center px-4 py-3 font-medium text-[#6B7280]">编辑</th>
              <th class="text-center px-4 py-3 font-medium text-[#6B7280]">删除</th>
              <th class="text-center px-4 py-3 font-medium text-[#6B7280]">审核</th>
              <th class="text-center px-4 py-3 font-medium text-[#6B7280]">导出</th>
              <th class="text-left px-4 py-3 font-medium text-[#6B7280]">操作</th>
            </tr></thead>
            <tbody>
              <tr v-for="r in roles" :key="r.name" class="border-b border-gray-100">
                <td class="px-4 py-3 font-medium text-gray-900">{{ r.name }}</td>
                <td v-for="p in r.perms" :key="p" class="text-center px-4 py-3">
                  <span :style="{color:p?'#16A34A':'#D1D5DB'}">{{ p ? '✓' : '—' }}</span>
                </td>
                <td class="px-4 py-3"><button @click="edit(r)" class="text-[#2563EB] text-[14px] cursor-pointer hover:underline">编辑</button></td>
              </tr>
            </tbody>
          </table>
        </div>
        <p class="text-[13px] text-gray-400 mt-3">原型展示 — 权限矩阵为静态演示数据</p>
      </main>
    </div>
  </div>
</template>
<script setup>
import { ref } from 'vue'
const roles = ref([
  { name:'系统管理员', perms:[true,true,true,true,true,true] },
  { name:'学校管理员', perms:[true,true,true,false,true,true] },
  { name:'学院管理员', perms:[true,false,false,false,true,false] },
  { name:'指导老师',   perms:[true,false,false,false,true,false] },
  { name:'辅导员',     perms:[true,false,false,false,false,false] },
  { name:'学生',       perms:[true,false,false,false,false,false] },
])
function edit(r) { alert(`编辑 ${r.name} 权限（演示）`) }
</script>
