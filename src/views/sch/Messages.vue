<!--
页面编号：SCH-NOTI-02
页面名称：学校消息中心
路由：/school/messages
当前阶段：阶段一-原型还原（复用 COM-NOTI-01 模板）
-->
<script setup>
import LayoutSchool from '../../layouts/LayoutSchool.vue'
import NotificationCenter from '../common/NotificationCenter.vue'
import { schDashboardStats } from '../../mock/sch.js'
</script>

<template>
  <LayoutSchool>
    <div class="p-[30px_40px]">
      <h1 class="text-[32px] font-bold text-black mb-[4px]">消息中心</h1>
      <p class="text-[16px] text-[#757575] mb-[24px]">系统通知与审核进度提醒</p>
      <NotificationCenter :messages="schDashboardStats.messages" />
    </div>
  </LayoutSchool>
</template>
