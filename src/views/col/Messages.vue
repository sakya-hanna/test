<!--
页面编号：COL-NOTI-02
页面名称：学院消息中心
路由：/college/messages
Figma Node：261:15
当前阶段：阶段一-原型还原（复用 COM-NOTI-01 模板）
-->
<script setup>
import LayoutCollege from '../../layouts/LayoutCollege.vue'
import NotificationCenter from '../common/NotificationCenter.vue'
import { colMessages } from '../../mock/col.js'
</script>

<template>
  <LayoutCollege>
    <div class="p-[30px_40px]">
      <h1 class="text-[32px] font-bold text-black mb-[4px]">消息中心</h1>
      <p class="text-[16px] text-[#757575] mb-[24px]">学院通知与系统消息</p>
      <NotificationCenter :messages="colMessages" />
    </div>
  </LayoutCollege>
</template>
