<!--
公共消息中心 — 独立路由页面
路由：/notifications、/notifications/:id
当前用途：各角色可从侧边栏"通知公告"进入
-->
<script setup>
import { ref } from 'vue'
import { useRoute } from 'vue-router'
import NotificationCenter from './common/NotificationCenter.vue'

const route = useRoute()

const messages = ref([
  { id: 1, title: '关于2026届毕业生实习工作安排的通知', type: 'notice', from: '教务处', time: '2026-06-15 09:00', read: false, content: '各学院：为做好2026届毕业生实习工作，现将有关事项通知如下：\n\n实习时间安排：2026年7月至2027年6月。各学院应根据专业培养方案要求，合理制定实习计划，确保实习质量。\n\n请各学院相关负责人在系统中完成实习计划的上报和审核工作。' },
  { id: 2, title: '暑假实习安全管理须知', type: 'alert', from: '保卫处', time: '2026-06-20 14:30', read: false, content: '暑假期间，请各学院加强对实习学生的安全教育和管理，具体要求如下：\n1. 加强交通安全教育，提醒学生遵守交通规则\n2. 注意实习场所安全，遵守企业安全规章制度\n3. 做好防暑降温工作，合理安排实习时间\n4. 保持通讯畅通，遇到问题及时向辅导员报告\n\n如遇紧急情况请拨打保卫处24小时值班电话。' },
  { id: 3, title: '关于企业入驻审核流程调整的通知', type: 'system', from: '教务处', time: '2026-06-12 11:00', read: true, content: '为进一步优化企业入驻审核流程，提高审核效率，现对审核流程进行如下调整：\n\n1. 企业注册后由学院进行初审\n2. 初审通过后自动流转至学校复核\n3. 审核周期原则上不超过5个工作日\n\n请各学院管理员和学校管理员知悉并遵照执行。' },
  { id: 4, title: '关于启用新版实习管理系统的通知', type: 'system', from: '系统管理员', time: '2026-06-01 08:00', read: true, content: '新版实习管理系统已于6月1日正式上线，请各用户通过统一身份认证登录后使用。\n\n新系统包含学生端、教师端、企业端、学院管理端、学校管理端、辅导员端共六个角色端口，实现了实习全流程的数字化管理。\n\n主要功能包括：实习申请与审核、周报月报提交与批阅、成绩评定与查看、企业注册与资质审核等。' },
])
</script>

<template>
  <div class="flex min-h-screen">
    <!-- 侧边栏 — 与 cou/Dashboard.vue 完全一致，通知公告选中 -->
    <aside class="w-[232px] min-h-screen flex-shrink-0 text-white flex flex-col" style="background:#00203D;">
      <div class="px-5 py-5 border-b border-white/10 text-lg font-semibold">实习管理系统</div>
      <nav class="flex-1 py-3 px-3 space-y-1">
        <router-link to="/counselor/dashboard" class="flex items-center px-4 py-3 rounded-lg text-[16px] text-white/80 hover:bg-white/10 transition-colors">指导工作台</router-link>
        <router-link to="/counselor/students" class="flex items-center px-4 py-3 rounded-lg text-[16px] text-white/80 hover:bg-white/10 transition-colors">我的学生</router-link>
        <router-link to="/counselor/process-feedback" class="flex items-center px-4 py-3 rounded-lg text-[16px] text-white/80 hover:bg-white/10 transition-colors">过程反馈</router-link>
        <router-link to="/counselor/evaluations" class="flex items-center px-4 py-3 rounded-lg text-[16px] text-white/80 hover:bg-white/10 transition-colors">学生评价</router-link>
        <span class="flex items-center px-4 py-3 rounded-lg text-[16px]" style="color:#C7D2FE;">通知公告</span>
      </nav>
    </aside>

    <!-- 右侧区域 -->
    <div class="flex-1 flex flex-col min-w-0" style="background:#F5F7FB;">
      <header class="h-[72px] bg-white flex items-center justify-between px-6 shadow-sm flex-shrink-0 border-b border-[#e5e7eb]">
        <span class="text-[14px]" style="color:#757575;">首页 / 消息中心</span>
        <router-link to="/login" class="text-[14px] font-medium" style="color:#FF383C;">登出</router-link>
      </header>
      <main class="flex-1 overflow-auto p-6">
        <h1 class="text-[32px] font-bold text-black mb-[4px]">消息中心</h1>
        <p class="text-[16px] text-[#757575] mb-[24px]">查看系统通知和重要公告</p>
        <div class="h-[calc(100vh-260px)]">
          <NotificationCenter :messages="messages" empty-text="暂无通知消息" />
        </div>
      </main>
    </div>
  </div>
</template>
