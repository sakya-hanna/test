<template>
  <div class="min-h-screen bg-[#F5F7FB] px-6 py-10">
    <div class="mx-auto max-w-[900px] rounded-[12px] bg-white p-8 shadow-sm">
      <div class="mb-6 flex items-center justify-between border-b border-[#e5e7eb] pb-4">
        <div>
          <h1 class="text-[28px] font-bold text-[#111827]">隐私政策</h1>
          <p class="mt-1 text-[14px] text-[#6b7280]">企业注册演示隐私说明</p>
        </div>
        <router-link to="/company/register" class="text-[14px] font-medium text-[#2563EB] hover:underline">返回注册</router-link>
      </div>

      <div class="space-y-5 text-[15px] leading-7 text-[#374151]">
        <section>
          <h2 class="mb-2 text-[18px] font-semibold text-[#111827]">一、信息收集</h2>
          <p>平台会在企业注册、资质审核和业务办理过程中收集联系人、企业基础资料、资质材料及审核流转信息。</p>
        </section>
        <section>
          <h2 class="mb-2 text-[18px] font-semibold text-[#111827]">二、信息使用</h2>
          <p>相关信息仅用于企业身份核验、校企合作管理、岗位发布审核、实习过程沟通以及必要的业务通知。</p>
        </section>
        <section>
          <h2 class="mb-2 text-[18px] font-semibold text-[#111827]">三、信息保护</h2>
          <p>平台将采取访问控制、权限隔离和日志记录等方式保护用户信息。演示阶段数据为模拟内容，不代表正式隐私条款。</p>
        </section>
      </div>
    </div>
  </div>
</template>
