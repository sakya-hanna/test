<!-- Figma: 26:1085 - COM-AUTH-06 - 手机登录页 402x874 -->
<template>
  <div class="min-h-screen bg-[#F0F4FF] flex items-center justify-center p-4">
    <div class="bg-white rounded-3xl shadow-xl p-8 w-full max-w-[375px]">
      <div class="text-center mb-8"><div class="w-12 h-12 bg-[#2563EB] rounded-xl flex items-center justify-center mx-auto mb-4"><span class="text-white text-xl font-bold">U</span></div><h2 class="text-lg font-bold text-gray-800">实习管理系统</h2><p class="text-xs text-gray-400 mt-1">移动端登录</p></div>
      <input v-model="acc" placeholder="请输入账号" class="w-full px-4 py-3 rounded-xl border border-gray-200 text-sm mb-3 focus:outline-none focus:border-[#2563EB]" />
      <input v-model="pwd" type="password" placeholder="请输入密码" class="w-full px-4 py-3 rounded-xl border border-gray-200 text-sm mb-5 focus:outline-none focus:border-[#2563EB]" />
      <button class="w-full py-3 bg-[#2563EB] text-white rounded-full font-medium text-sm mb-4 hover:opacity-90">登 录</button>
      <div class="text-center text-xs text-gray-400"><router-link to="/login" class="hover:underline">电脑端登录</router-link></div>
    </div>
  </div>
</template>
<script setup>import { ref } from 'vue'; const acc=ref(''),pwd=ref('')</script>
