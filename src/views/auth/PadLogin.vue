<!-- Figma: 26:1114 - COM-AUTH-07 - 平板登录页 1280x800 -->
<template>
  <div class="min-h-screen bg-[#F0F4FF] flex items-center justify-center p-6">
    <div class="bg-white rounded-3xl shadow-xl p-10 w-full max-w-[600px]">
      <div class="text-center mb-8"><div class="w-14 h-14 bg-[#2563EB] rounded-2xl flex items-center justify-center mx-auto mb-4"><span class="text-white text-2xl font-bold">U</span></div><h2 class="text-xl font-bold text-gray-800">实习管理系统</h2><p class="text-sm text-gray-400 mt-1">平板端登录</p></div>
      <div class="max-w-[380px] mx-auto"><input v-model="acc" placeholder="请输入账号" class="w-full px-4 py-3 rounded-xl border border-gray-200 text-sm mb-4 focus:outline-none focus:border-[#2563EB]" /><input v-model="pwd" type="password" placeholder="请输入密码" class="w-full px-4 py-3 rounded-xl border border-gray-200 text-sm mb-6 focus:outline-none focus:border-[#2563EB]" /><button class="w-full py-3 bg-[#2563EB] text-white rounded-full font-medium text-sm mb-4 hover:opacity-90">登 录</button><div class="text-center text-sm text-gray-400"><router-link to="/login" class="hover:underline">电脑端登录</router-link></div></div>
    </div>
  </div>
</template>
<script setup>import { ref } from 'vue'; const acc=ref(''),pwd=ref('')</script>
