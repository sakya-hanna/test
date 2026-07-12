<!-- Figma: 164:132 - ENT-ONB-05 - 企业门户-Landing -->
<template>
  <div class="bg-white min-h-screen">

    <!-- ====== 顶栏 72px ====== -->
    <header class="fixed top-0 left-0 right-0 h-[72px] bg-white/95 backdrop-blur-sm shadow-sm z-50">
      <div class="w-[1440px] mx-auto h-full flex items-center px-[60px]">
        <!-- Logo -->
        <span class="text-[24px] font-extrabold text-[#00203D] mr-2">无锡学院</span>
        <span class="text-[14px] text-[#6B7280] mt-0.5">实习实践管理平台</span>
        <!-- 导航 -->
        <nav class="flex items-center gap-[100px] ml-[280px] text-[15px]">
          <span class="font-semibold text-[#2563EB]">首页</span>
          <span class="text-[#374151]">合作企业</span>
          <span class="text-[#374151]">实习项目</span>
          <span class="text-[#374151]">关于我们</span>
        </nav>
        <!-- 登录按钮 -->
        <router-link to="/login"
          class="ml-auto w-[120px] h-[44px] bg-[#2563EB] rounded-[24px] flex items-center justify-center text-[14px] font-medium text-white hover:opacity-90">
          企业登录
        </router-link>
      </div>
    </header>

    <!-- ====== Hero 横幅 600px ====== -->
    <section class="mt-[72px] h-[600px] bg-[#00203D] relative overflow-hidden">
      <div class="absolute inset-0 bg-[rgba(37,99,235,0.3)]"></div>
      <div class="relative w-[1440px] mx-auto px-[120px] pt-[128px]">
        <h1 class="text-[52px] font-extrabold text-white leading-tight mb-4">
          携手无锡学院<br/>共育未来英才
        </h1>
        <p class="text-[18px] text-[#C7D2FE] mb-3">
          无锡学院实习实践管理平台 — 连接高校与企业，打造产教融合新生态
        </p>
        <p class="text-[18px] text-[#C7D2FE] mb-10">
          已与300+企业建立合作关系，每年为2000+学生提供优质实习岗位
        </p>
        <router-link to="/company/register"
          class="inline-flex items-center justify-center w-[180px] h-[52px] bg-white rounded-[28px] text-[16px] font-semibold text-[#2563EB] hover:opacity-90">
          立即合作
        </router-link>
      </div>
    </section>

    <!-- ====== 数据统计栏 120px ====== -->
    <section class="h-[120px] bg-[#F9FAFB]">
      <div class="w-[1440px] mx-auto h-full flex items-center justify-around px-[120px]">
        <div class="text-center">
          <div class="text-[36px] font-extrabold text-[#2563EB]">300+</div>
          <div class="text-[14px] text-[#6B7280]">合作企业</div>
        </div>
        <div class="text-center">
          <div class="text-[36px] font-extrabold text-[#2563EB]">2,000+</div>
          <div class="text-[14px] text-[#6B7280]">实习学生/年</div>
        </div>
        <div class="text-center">
          <div class="text-[36px] font-extrabold text-[#2563EB]">95%</div>
          <div class="text-[14px] text-[#6B7280]">企业满意度</div>
        </div>
        <div class="text-center">
          <div class="text-[36px] font-extrabold text-[#2563EB]">50+</div>
          <div class="text-[14px] text-[#6B7280]">合作专业</div>
        </div>
      </div>
    </section>

    <!-- ====== 为什么选择无锡学院 ====== -->
    <section class="py-16">
      <div class="w-[1440px] mx-auto px-[120px]">
        <h2 class="text-[36px] font-bold text-black text-center mb-2">为什么选择无锡学院</h2>
        <p class="text-[16px] text-[#6B7280] text-center mb-10">我们致力于为企业提供最优质的人才对接服务</p>
        <div class="flex gap-[25px] justify-between">
          <div class="w-[270px] h-[180px] bg-white rounded-[16px] border border-[rgba(0,0,0,0.1)] p-8">
            <h3 class="text-[20px] font-semibold text-[#00203D] mb-3">优质生源</h3>
            <p class="text-[14px] text-[#6B7280] leading-relaxed">经过严格选拔和<br/>专业培养的优秀学生</p>
          </div>
          <div class="w-[270px] h-[180px] bg-white rounded-[16px] border border-[rgba(0,0,0,0.1)] p-8">
            <h3 class="text-[20px] font-semibold text-[#00203D] mb-3">全程管理</h3>
            <p class="text-[14px] text-[#6B7280] leading-relaxed">学校全程跟踪实习<br/>确保实习质量</p>
          </div>
          <div class="w-[270px] h-[180px] bg-white rounded-[16px] border border-[rgba(0,0,0,0.1)] p-8">
            <h3 class="text-[20px] font-semibold text-[#00203D] mb-3">精准匹配</h3>
            <p class="text-[14px] text-[#6B7280] leading-relaxed">智能匹配企业需求<br/>与学生专业方向</p>
          </div>
          <div class="w-[270px] h-[180px] bg-white rounded-[16px] border border-[rgba(0,0,0,0.1)] p-8">
            <h3 class="text-[20px] font-semibold text-[#00203D] mb-3">高效沟通</h3>
            <p class="text-[14px] text-[#6B7280] leading-relaxed">专属辅导员对接<br/>快速响应企业诉求</p>
          </div>
        </div>
      </div>
    </section>

    <!-- ====== 合作企业 ====== -->
    <section class="py-16 bg-white">
      <div class="w-[1440px] mx-auto px-[120px]">
        <h2 class="text-[36px] font-bold text-black text-center mb-2">合作企业</h2>
        <p class="text-[16px] text-[#6B7280] text-center mb-10">部分与我们建立长期合作关系的优秀企业</p>
        <div class="grid grid-cols-6 gap-5">
          <div v-for="p in partners" :key="p"
            class="h-[80px] bg-[#F9FAFB] rounded-[12px] flex items-center justify-center text-[18px] font-semibold text-[#00203D]">
            {{ p }}
          </div>
        </div>
      </div>
    </section>

    <!-- ====== 校企合作成果 ====== -->
    <section class="h-[280px] bg-[#00203D]">
      <div class="w-[1440px] mx-auto h-full px-[120px] flex flex-col justify-center">
        <h2 class="text-[36px] font-bold text-white text-center mb-10">校企合作成果</h2>
        <div class="flex justify-around text-center">
          <div>
            <div class="text-[36px] font-extrabold text-white">500+</div>
            <div class="text-[14px] text-[#C7D2FE]">累计合作企业</div>
          </div>
          <div>
            <div class="text-[36px] font-extrabold text-white">10,000+</div>
            <div class="text-[14px] text-[#C7D2FE]">累计实习学生</div>
          </div>
          <div>
            <div class="text-[36px] font-extrabold text-white">98.5%</div>
            <div class="text-[14px] text-[#C7D2FE]">就业转化率</div>
          </div>
          <div>
            <div class="text-[36px] font-extrabold text-white">50+</div>
            <div class="text-[14px] text-[#C7D2FE]">校企合作项目</div>
          </div>
        </div>
      </div>
    </section>

    <!-- ====== 底部 CTA ====== -->
    <section class="py-20 text-center">
      <h2 class="text-[32px] font-bold text-black mb-3">准备好与我们合作了吗？</h2>
      <p class="text-[16px] text-[#6B7280] mb-8">立即注册企业账号，发布实习需求，对接优秀学子</p>
      <router-link to="/company/register"
        class="inline-flex items-center justify-center w-[200px] h-[56px] bg-[#2563EB] rounded-[28px] text-[16px] font-semibold text-white hover:opacity-90">
        立即注册企业账号
      </router-link>
    </section>

    <!-- ====== 页脚 ====== -->
    <footer class="h-[260px] bg-[#111827] text-white">
      <div class="w-[1440px] mx-auto h-full px-[120px] py-14 flex">
        <!-- 左侧 -->
        <div class="w-[380px]">
          <div class="text-[24px] font-extrabold mb-3">无锡学院</div>
          <div class="text-[13px] text-[#9CA3AF] leading-relaxed">
            <p>实习实践管理平台 © 2026</p>
            <p>地址：无锡市XX区XX路XX号</p>
            <p>联系电话：0510-XXXX-XXXX</p>
          </div>
        </div>
        <!-- 快速链接 -->
        <div class="w-[180px]">
          <div class="text-[16px] font-semibold mb-4">快速链接</div>
          <div class="flex flex-col gap-2 text-[13px] text-[#9CA3AF]">
            <span>首页</span><span>实习项目</span><span>关于我们</span>
          </div>
        </div>
        <!-- 合作服务 -->
        <div class="w-[180px]">
          <div class="text-[16px] font-semibold mb-4">合作服务</div>
          <div class="flex flex-col gap-2 text-[13px] text-[#9CA3AF]">
            <router-link to="/company/register" class="hover:text-white">企业注册</router-link>
            <span>发布需求</span><span>合作咨询</span>
          </div>
        </div>
        <!-- 联系我们 -->
        <div class="w-[180px]">
          <div class="text-[16px] font-semibold mb-4">联系我们</div>
          <div class="flex flex-col gap-2 text-[13px] text-[#9CA3AF]">
            <span>电话咨询</span><span>在线留言</span><span>邮件联系</span>
          </div>
        </div>
      </div>
    </footer>

  </div>
</template>

<script setup>
const partners = [
  '无锡科技','南京智造','上海数据','苏州软件','杭州云','常州装备',
  '北京创新','深圳互联','广州数科','成都软通','武汉光谷','西安芯科'
]
</script>
