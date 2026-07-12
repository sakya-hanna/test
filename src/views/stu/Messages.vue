<!--
页面编号：COM-NOTI-01
页面名称：通知公告（学生端消息中心）
路由：/stu/messages
当前阶段：阶段一-原型还原（复用 COM-NOTI-01 模板）
-->
<script setup>
import LayoutBackend from '../../layouts/LayoutBackend.vue'
import NotificationCenter from '../common/NotificationCenter.vue'
import { dashboardStats } from '../../mock/stu.js'
</script>

<template>
  <LayoutBackend>
    <div class="p-[30px_40px]">
      <h1 class="text-[32px] font-bold text-black mb-[4px]">通知公告</h1>
      <p class="text-[16px] text-[#757575] mb-[24px]">系统通知与消息</p>
      <NotificationCenter :messages="dashboardStats.messages" />
    </div>
  </LayoutBackend>
</template>
