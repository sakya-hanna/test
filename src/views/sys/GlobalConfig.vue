<!-- SYS-CFG-03 全局配置 -->
<template>
  <div class="flex min-h-screen">
    <aside class="w-[232px] min-h-screen flex-shrink-0 text-white flex flex-col" style="background:#00203D;">
      <div class="px-5 py-5 border-b border-white/10 text-lg font-semibold">实习管理系统</div>
      <nav class="flex-1 py-3 px-3 space-y-1">
        <router-link to="/admin/dashboard" class="flex items-center px-4 py-3 rounded-lg text-[16px] text-white/80 hover:bg-white/10">工作台</router-link>
        <router-link to="/admin/users" class="flex items-center px-4 py-3 rounded-lg text-[16px] text-white/80 hover:bg-white/10">用户管理</router-link>
        <router-link to="/admin/roles" class="flex items-center px-4 py-3 rounded-lg text-[16px] text-white/80 hover:bg-white/10">权限配置</router-link>
        <router-link to="/admin/settings" class="flex items-center px-4 py-3 rounded-lg text-[16px]" style="color:#C7D2FE;">全局配置</router-link>
        <router-link to="/admin/logs" class="flex items-center px-4 py-3 rounded-lg text-[16px] text-white/80 hover:bg-white/10">系统日志</router-link>
        <router-link to="/admin/errors" class="flex items-center px-4 py-3 rounded-lg text-[16px] text-white/80 hover:bg-white/10">异常处理</router-link>
      </nav>
    </aside>
    <div class="flex-1 flex flex-col" style="background:#F5F7FB;">
      <header class="h-[72px] bg-white flex items-center justify-between px-6 shadow-sm flex-shrink-0">
        <span class="text-[14px]" style="color:#757575;">系统管理 / 全局配置</span>
        <router-link to="/login" class="text-[14px] font-medium" style="color:#FF383C;">登出</router-link>
      </header>
      <main class="flex-1 overflow-auto p-6">
        <h2 class="text-[32px] font-bold text-gray-900 mb-1">全局配置</h2>
        <p class="text-[16px] mb-6" style="color:#757575;">管理系统全局参数与配置项</p>

        <div class="grid grid-cols-2 gap-5">
          <div v-for="g in groups" :key="g.title" class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] p-6">
            <h3 class="text-[18px] font-semibold text-gray-900 mb-4">{{ g.title }}</h3>
            <div v-for="item in g.items" :key="item.key" class="flex items-center justify-between py-3 border-b border-gray-100 last:border-0">
              <div><div class="text-[14px] text-gray-700">{{ item.label }}</div><div class="text-[12px] text-[#9CA3AF]">{{ item.desc }}</div></div>
              <span class="text-[14px] font-medium" :style="{color:item.color||'#374151'}">{{ item.value }}</span>
            </div>
          </div>
        </div>
        <p class="text-[13px] text-gray-400 mt-4">原型展示 — 配置项为静态演示，不可修改</p>
      </main>
    </div>
  </div>
</template>
<script setup>
import { ref } from 'vue'
const groups = ref([
  { title:'系统参数', items:[
    { key:'sname', label:'系统名称', value:'实习管理系统', desc:'系统对外展示名称' },
    { key:'version', label:'当前版本', value:'v1.0.0-prototype', desc:'系统版本号', color:'#2563EB' },
    { key:'maxLogin', label:'最大登录尝试', value:'5 次', desc:'连续登录失败锁定阈值' },
  ]},
  { title:'通知设置', items:[
    { key:'sms', label:'短信通知', value:'已启用', desc:'审核结果短信通知', color:'#16A34A' },
    { key:'email', label:'邮件通知', value:'已启用', desc:'系统消息邮件提醒', color:'#16A34A' },
    { key:'wechat', label:'微信通知', value:'未启用', desc:'微信小程序消息推送', color:'#9CA3AF' },
  ]},
  { title:'学期管理', items:[
    { key:'term', label:'当前学期', value:'2026 春季学期', desc:'当前教学学期' },
    { key:'start', label:'学期开始', value:'2026-03-01', desc:'学期起始日期' },
    { key:'end', label:'学期结束', value:'2026-07-15', desc:'学期结束日期' },
  ]},
  { title:'审核流程', items:[
    { key:'col', label:'学院审核', value:'已开启', desc:'企业入驻需学院初审', color:'#16A34A' },
    { key:'sch', label:'学校复核', value:'已开启', desc:'学院通过后学校复核', color:'#16A34A' },
    { key:'auto', label:'自动通过', value:'未开启', desc:'免审自动通过', color:'#9CA3AF' },
  ]},
])
</script>
