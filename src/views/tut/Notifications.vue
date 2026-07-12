<!--
页面编号：TUT-NOTI-01
页面名称：指导老师消息中心
路由：/teacher/notifications
Figma Node：260:15
当前阶段：阶段一-原型还原（复用 COM-NOTI-01 模板）
-->
<script setup>
import LayoutTeacher from '../../layouts/LayoutTeacher.vue'
import NotificationCenter from '../common/NotificationCenter.vue'
import { tutDashboardStats } from '../../mock/tut.js'
</script>

<template>
  <LayoutTeacher>
    <div class="p-[30px_40px]">
      <h1 class="text-[32px] font-bold text-black mb-[4px]">消息中心</h1>
      <p class="text-[16px] text-[#757575] mb-[24px]">系统通知与审核提醒</p>
      <NotificationCenter :messages="tutDashboardStats.messages" />
    </div>
  </LayoutTeacher>
</template>
