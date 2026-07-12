<!-- Figma: 26:474 - COM-AUTH-01 - PC 登录页 1440x1024 -->
<template>
  <div class="w-full h-screen flex flex-col items-center justify-center bg-cover bg-center gap-6" style="background-image:url('/login-bg.png');">
    <!-- LOGO 228x69 -->
    <img src="/logo.png" class="w-[228px] h-[69px]" alt="LOGO" />
    <!-- Title fs:48 fw:700 color:#ffffff -->
    <h1 class="text-[48px] font-bold text-white">实习管理系统</h1>

    <!-- Form Log In 522px wide bg=rgba(0,0,0,0.5) radius=8px -->
    <div class="w-[522px] rounded-[8px] p-12" style="background:rgba(0,0,0,0.5);">
      <!-- 企业账号 Label fs:16 color:#ffffff -->
      <label class="block text-[16px] mb-1.5" style="color:#ffffff;">企业账号</label>
      <!-- Input placeholder color:#B3B3B3 -->
      <input v-model="account" type="text" placeholder="请输入账号"
        class="w-full px-4 py-3 rounded-[8px] border border-white/20 text-[16px] bg-white/10 mb-4 focus:outline-none focus:border-[#349CFE]"
        style="color:#ffffff;" />

      <!-- 密码 Label fs:16 color:#ffffff -->
      <label class="block text-[16px] mb-1.5" style="color:#ffffff;">密码</label>
      <input v-model="password" :type="show ? 'text' : 'password'" placeholder="请输入密码"
        class="w-full px-4 py-3 rounded-[8px] border border-white/20 text-[16px] bg-white/10 mb-6 focus:outline-none focus:border-[#349CFE]"
        style="color:#ffffff;" />

      <!-- 登录按钮 bg=#349CFE radius=8px text=#F5F5F5 -->
      <button @click="go"
        class="w-full py-3 rounded-[8px] text-[16px] mb-6 transition-all hover:opacity-90"
        style="background:#349CFE; color:#F5F5F5;">登录</button>

      <div class="mb-6 pt-4 border-t border-white/20">
        <p class="text-[13px] mb-3" style="color:rgba(255,255,255,0.78);">企业身份演示入口</p>
        <div class="grid grid-cols-2 gap-2">
          <router-link
            v-for="role in companyRoleLinks"
            :key="role.label"
            :to="role.path"
            class="h-[36px] rounded-[8px] flex items-center justify-center text-[13px] font-medium text-white bg-white/10 border border-white/20 hover:bg-white/20 transition-colors"
          >
            {{ role.label }}
          </router-link>
        </div>
      </div>

      <!-- 底部链接 fs:16 color:#349CFE -->
      <div class="flex justify-between text-[16px]">
        <router-link to="/sso/account" class="hover:underline" style="color:#349CFE;">统一身份认证</router-link>
        <router-link to="/portal" class="hover:underline" style="color:#349CFE;">企业注册</router-link>
        <router-link to="/forgot-password" class="hover:underline" style="color:#349CFE;">忘记密码</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
const r = useRouter()
const account = ref('')
const password = ref('')
const show = ref(false)
const companyRoleLinks = [
  { label: '企业管理员', path: '/company/admin' },
  { label: '企业联系人', path: '/company/contact' },
  { label: '招聘负责人', path: '/company/recruiter' },
  { label: '企业指导老师', path: '/company/mentor' },
]
function go() { r.push('/company/contact') }
</script>
