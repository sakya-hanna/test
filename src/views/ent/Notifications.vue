<!--
页面编号：ENT-NOTI-01
页面名称：企业消息中心
路由：/company/notifications
Figma Node：259:15
-->
<script setup>
import CompanySidebar from '../../components/CompanySidebar.vue'
import NotificationCenter from '../common/NotificationCenter.vue'
import { entMessages } from '../../mock/ent.js'
</script>

<template>
  <div class="flex min-h-screen">
    <!-- 侧边栏 -->
    <CompanySidebar />

    <!-- 右侧内容 -->
    <div class="flex-1 flex flex-col" style="background:#F5F7FB;">
      <header class="h-[72px] bg-white flex items-center justify-between px-6 shadow-sm flex-shrink-0">
        <span class="text-[14px]" style="color:#757575;">企业管理 / 企业消息中心</span>
        <div class="flex items-center gap-4">
          <div class="flex items-center gap-2"><div class="w-10 h-10 rounded-full flex items-center justify-center text-xs font-medium text-white" style="background:#2563EB;">华</div><div><div class="text-[14px] font-medium text-gray-900">华为技术有限公司</div><div class="text-[12px]" style="color:#16A34A;">企业用户</div></div></div>
          <router-link to="/login" class="text-[14px] font-medium" style="color:#FF383C;">登出</router-link>
        </div>
      </header>

      <main class="flex-1 overflow-auto p-6">
        <div class="mb-6">
          <p class="text-[14px]" style="color:#757575;">企业管理 / 企业消息中心</p>
          <h2 class="text-[32px] font-bold text-gray-900 mt-1">企业消息中心</h2>
          <p class="text-[16px] mt-1" style="color:#757575;">查看系统通知、学生申请提醒和学校公告</p>
        </div>
        <NotificationCenter :messages="entMessages" empty-text="暂无企业相关消息" />
      </main>
    </div>
  </div>
</template>
