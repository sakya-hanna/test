<!-- SYS-CFG-01 用户管理 -->
<template>
  <div class="flex min-h-screen">
    <aside class="w-[232px] min-h-screen flex-shrink-0 text-white flex flex-col" style="background:#00203D;">
      <div class="px-5 py-5 border-b border-white/10 text-lg font-semibold">实习管理系统</div>
      <nav class="flex-1 py-3 px-3 space-y-1">
        <router-link to="/admin/dashboard" class="flex items-center px-4 py-3 rounded-lg text-[16px] text-white/80 hover:bg-white/10">工作台</router-link>
        <router-link to="/admin/users" class="flex items-center px-4 py-3 rounded-lg text-[16px]" style="color:#C7D2FE;">用户管理</router-link>
        <router-link to="/admin/roles" class="flex items-center px-4 py-3 rounded-lg text-[16px] text-white/80 hover:bg-white/10">权限配置</router-link>
        <router-link to="/admin/settings" class="flex items-center px-4 py-3 rounded-lg text-[16px] text-white/80 hover:bg-white/10">全局配置</router-link>
        <router-link to="/admin/logs" class="flex items-center px-4 py-3 rounded-lg text-[16px] text-white/80 hover:bg-white/10">系统日志</router-link>
        <router-link to="/admin/errors" class="flex items-center px-4 py-3 rounded-lg text-[16px] text-white/80 hover:bg-white/10">异常处理</router-link>
      </nav>
    </aside>
    <div class="flex-1 flex flex-col" style="background:#F5F7FB;">
      <header class="h-[72px] bg-white flex items-center justify-between px-6 shadow-sm flex-shrink-0">
        <span class="text-[14px]" style="color:#757575;">系统管理 / 用户管理</span>
        <router-link to="/login" class="text-[14px] font-medium" style="color:#FF383C;">登出</router-link>
      </header>
      <main class="flex-1 overflow-auto p-6">
        <h2 class="text-[32px] font-bold text-gray-900 mb-1">用户管理</h2>
        <p class="text-[16px] mb-6" style="color:#757575;">管理系统用户账号与基本信息</p>

        <!-- 筛选 -->
        <div class="flex items-center gap-4 mb-4">
          <input v-model="search" placeholder="搜索用户名/姓名..."
            class="w-[260px] px-4 py-2 rounded-[8px] border border-[rgba(0,0,0,0.16)] text-[14px] bg-white focus:outline-none focus:border-[#2563EB]" />
          <select v-model="roleFilter" class="px-4 py-2 rounded-[8px] border border-[rgba(0,0,0,0.16)] text-[14px] bg-white">
            <option value="">全部角色</option>
            <option>学生</option><option>教师</option><option>辅导员</option><option>学院管理员</option><option>学校管理员</option><option>系统管理员</option>
          </select>
          <span class="text-[13px] text-gray-400">原型展示 — 操作按钮为演示</span>
        </div>

        <!-- 表格 -->
        <div class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] overflow-hidden">
          <table class="w-full text-[14px]">
            <thead><tr style="background:#F5F7FB;">
              <th class="text-left px-4 py-3 font-medium text-[#6B7280]">用户名</th>
              <th class="text-left px-4 py-3 font-medium text-[#6B7280]">姓名</th>
              <th class="text-left px-4 py-3 font-medium text-[#6B7280]">角色</th>
              <th class="text-left px-4 py-3 font-medium text-[#6B7280]">所属学院</th>
              <th class="text-left px-4 py-3 font-medium text-[#6B7280]">状态</th>
              <th class="text-left px-4 py-3 font-medium text-[#6B7280]">操作</th>
            </tr></thead>
            <tbody>
              <tr v-for="u in filteredUsers" :key="u.id" class="border-b border-gray-100 hover:bg-gray-50">
                <td class="px-4 py-3 font-medium text-gray-900">{{ u.username }}</td>
                <td class="px-4 py-3 text-gray-600">{{ u.name }}</td>
                <td class="px-4 py-3 text-gray-600">{{ u.role }}</td>
                <td class="px-4 py-3 text-gray-600">{{ u.college }}</td>
                <td class="px-4 py-3"><span class="px-2.5 py-1 rounded-full text-xs font-medium"
                  :style="u.active?{color:'#16A34A',background:'rgba(20,141,61,0.15)'}:{color:'#FF383C',background:'rgba(255,56,60,0.15)'}">{{ u.active?'启用':'禁用' }}</span></td>
                <td class="px-4 py-3"><button @click="edit(u)" class="text-[#2563EB] text-[14px] cursor-pointer hover:underline">编辑</button></td>
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
const search = ref(''), roleFilter = ref('')
const users = ref([
  { id:1,username:'20231001',name:'张同学',role:'学生',college:'计算机学院',active:true },
  { id:2,username:'T2023001',name:'李老师',role:'教师',college:'计算机学院',active:true },
  { id:3,username:'C2023001',name:'王辅导员',role:'辅导员',college:'电子学院',active:true },
  { id:4,username:'A2023001',name:'张院长',role:'学院管理员',college:'计算机学院',active:true },
  { id:5,username:'S2023001',name:'王处长',role:'学校管理员',college:'—',active:true },
  { id:6,username:'SA2023001',name:'赵工',role:'系统管理员',college:'—',active:false },
])
const filteredUsers = computed(() => users.value.filter(u =>
  (!search.value || u.name.includes(search.value) || u.username.includes(search.value)) &&
  (!roleFilter.value || u.role === roleFilter.value)
))
function edit(u) { alert(`编辑用户 ${u.name}（演示）`) }
</script>
