<!-- Figma: 26:1044 - COM-AUTH-08 - 企业注册入口 -->
<template>
  <div class="w-full h-screen flex flex-col items-center justify-center bg-cover bg-center gap-6" style="background-image:url('/forgot-bg.png');">
    <!-- Form 432px wide bg=rgba(0,0,0,0.5) radius=8 -->
    <div class="w-[432px] rounded-[8px] px-10 py-8" style="background:rgba(0,0,0,0.5);">
      <div class="flex justify-center mb-4">
        <div class="w-[179px] h-[54px] bg-[#2563EB] rounded-xl flex items-center justify-center">
          <img src="/logo.png" class="w-[179px] h-[54px]" alt="LOGO" />
        </div>
      </div>
      <h2 class="text-center text-[16px] font-bold mb-6" style="color:#ffffff;">企业账号注册</h2>

      <input v-model="f.phone" placeholder="请输入您的手机号"
        class="w-full px-4 py-3.5 rounded-[8px] border border-white/20 text-[16px] bg-white/10 mb-3 focus:outline-none focus:border-[#349CFE]"
        style="color:#B3B3B3;" />
      <div class="mb-3 flex gap-2">
        <input v-model="f.sms" placeholder="请输入验证码"
          class="flex-1 px-4 py-3.5 rounded-[8px] border border-white/20 text-[16px] bg-white/10 focus:outline-none focus:border-[#349CFE]"
          style="color:#B3B3B3;" />
        <button @click="send" :disabled="c>0"
          class="px-4 py-3.5 rounded-[8px] text-[16px] font-medium whitespace-nowrap"
          :style="{background:c>0?'#D1D5DB':'#349CFE', color:c>0?'#9CA3AF':'#ffffff'}">发送验证码</button>
      </div>
      <input v-model="f.pwd" type="password" placeholder="请输入密码"
        class="w-full px-4 py-3.5 rounded-[8px] border border-white/20 text-[16px] bg-white/10 mb-3 focus:outline-none focus:border-[#349CFE]"
        style="color:#B3B3B3;" />
      <input v-model="f.cpwd" type="password" placeholder="请确认密码"
        class="w-full px-4 py-3.5 rounded-[8px] border border-white/20 text-[16px] bg-white/10 mb-6 focus:outline-none focus:border-[#349CFE]"
        style="color:#B3B3B3;" />

      <router-link to="/company/register"
        class="block w-full py-3 rounded-[8px] text-[16px] font-bold text-center mb-4 transition-all hover:opacity-90"
        style="background:#9A05BC; color:#ffffff;">注册</router-link>

      <div class="text-center">
        <router-link to="/login" class="text-[16px] hover:underline" style="color:#349CFE;">返回登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
const f=reactive({phone:'',sms:'',pwd:'',cpwd:''})
const c=ref(0)
function send(){if(!f.phone)return;c.value=60;const t=setInterval(()=>{c.value--;if(c.value<=0)clearInterval(t)},1000)}
</script>
