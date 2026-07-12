<!-- Figma: 26:1017 - COM-AUTH-05 - SSO 扫码登录 -->
<template>
  <div class="w-full h-screen flex flex-col items-center justify-center bg-cover bg-center gap-6" style="background-image:url('/sso-left-bg.png');">
    <div class="flex">
      <!-- 左：Form 432px -->
      <div class="w-[432px] min-h-[627px] rounded-[8px] px-10 py-8 flex flex-col items-center" style="background:rgba(0,0,0,0.5);">
      <img src="/logo.png" class="w-[179px] h-[54px] mx-auto mb-4" alt="LOGO" />
      <p class="text-center text-[16px] font-bold mb-6" style="color:#ffffff;">统一身份认证</p>

      <!-- Tabs -->
      <div class="flex gap-0 mb-2 w-full">
        <router-link to="/sso/qr" class="flex-1 text-center text-[20px] font-bold" style="color:#ffffff;">扫码登录</router-link>
        <router-link to="/sso/sms" class="flex-1 text-center text-[20px]" style="color:#ffffff;">短信登录</router-link>
        <router-link to="/sso/account" class="flex-1 text-center text-[20px]" style="color:#ffffff;">账号登录</router-link>
      </div>
      <hr class="mb-6 w-full" style="border-color:rgba(255,255,255,0.3);" />

      <!-- 二维码 188x188 -->
      <img src="/sso-qr-big.png" class="w-[188px] h-[188px] mb-6" alt="二维码" />

      <!-- 微信扫码登录按钮 179x40 bg=white stroke=white r=8 -->
      <div class="w-[179px] h-[40px] rounded-[8px] flex items-center justify-center" style="background:#ffffff; border:1px solid #ffffff;">
        <img src="/sso-image.png" class="w-[22px] h-[20px] mr-2" alt="" />
        <span class="text-[16px]" style="color:#1E1E1E;">微信扫码登录</span>
      </div>
      <AuthRoleDemoLinks />
    </div>

      <!-- 右：description 432px -->
      <div class="w-[432px] min-h-[627px] rounded-[8px] px-8 py-10 flex flex-col" style="background:rgba(20,18,24,0.25);">
        <h3 class="text-[20px] font-bold mb-6" style="color:#ffffff;">统一身份认证系统</h3>
        <p class="text-[14px] mb-4 leading-relaxed" style="color:#ffffff;">初次登录前，请进行账号激活。激活后，使用扫码/短信登录，系统无初始密码。</p>
        <div class="space-y-3 text-[14px] leading-relaxed flex-1" style="color:#ffffff;">
          <p><span class="font-medium">1. 扫码登录：</span>使用微信扫码，在弹出页面点击"确认登录"即可完成登录。</p>
          <p><span class="font-medium">2. 短信登录：</span>输入手机号后点击"发送验证码"，收到验证码后输入并登录。</p>
          <p><span class="font-medium">3. 账号密码登录：</span>输入账号（教师工号/学生学号）、密码和验证码后登录。</p>
        </div>
        <span class="text-[14px] text-blue-300 mt-4">使用说明：https://www.cwxu.edu.cn/info/1039/19697.htm</span>
        <div class="mt-6 flex flex-col items-center">
          <img src="/sso-qrcode.png" class="w-[84px] h-[84px] rounded-xl mb-3" alt="小程序码" />
          <span class="text-[10px] font-bold" style="color:#ffffff;">无锡学院自助服务小程序</span>
        </div>
        <router-link to="/login" class="text-center text-[16px] mt-6 hover:underline" style="color:#349CFE;">返回登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import AuthRoleDemoLinks from '../../components/AuthRoleDemoLinks.vue'
</script>
