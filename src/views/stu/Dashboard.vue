<!--
页面编号：STU-DASH-01
页面名称：学生工作台
路由：/stu/dashboard
Figma Node：171:15
当前阶段：阶段一-原型还原
-->
<script setup>
import LayoutBackend from '../../layouts/LayoutBackend.vue'
import { dashboardStats } from '../../mock/stu.js'
import { useRouter } from 'vue-router'

const router = useRouter()
const stats = dashboardStats

function go(link) {
  // TODO: 部分路由尚未注册，先保留链接行为
  if (link) router.push(link)
}
</script>

<template>
  <LayoutBackend>
    <div class="p-[30px_40px]">

      <!-- ===== 页面标题 ===== -->
      <h1 class="text-[32px] font-bold text-black mb-[4px]">学生工作台</h1>
      <p class="text-[16px] text-[#757575] mb-[24px]">
        欢迎回来，{{ stats.currentInternship.company ? '你正在进行' + stats.currentInternship.type : '请发起实习申请' }}
      </p>

      <!-- ===== 统计卡片组 ===== -->
      <div class="grid grid-cols-4 gap-[22px] mb-[25px]">
        <div
          v-for="card in stats.statCards"
          :key="card.label"
          class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] p-[20px_24px] flex flex-col justify-between min-h-[100px]"
        >
          <span class="text-[14px] text-[#757575]">{{ card.label }}</span>
          <span class="text-[36px] font-bold" :style="{ color: card.color }">{{ card.value }}</span>
        </div>
      </div>

      <!-- ===== 双栏区域 上排 ===== -->
      <div class="grid grid-cols-2 gap-[22px] mb-[22px]">

        <!-- 实习状态卡片（含进度条） -->
        <div class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] p-[24px]">
          <h2 class="text-[18px] font-semibold mb-[4px]">当前实习</h2>
          <p class="text-[14px] text-[#757575] mb-[16px]">{{ stats.currentInternship.type }} · {{ stats.currentInternship.status }}</p>

          <!-- 类型标签 -->
          <span class="inline-block px-[12px] py-[2px] rounded-[12px] text-[11px] font-medium bg-[rgba(37,99,235,0.1)] text-[#2563eb] mb-[12px]">
            {{ stats.currentInternship.type }}
          </span>

          <!-- 企业/岗位 -->
          <div class="text-[14px] mb-[4px]">
            <span class="text-[#757575]">企业：</span>{{ stats.currentInternship.company }}
            <span class="text-[#757575] ml-[16px]">岗位：</span>{{ stats.currentInternship.position }}
          </div>
          <!-- 起止时间 -->
          <div class="text-[14px] mb-[40px]">
            <span class="text-[#757575]">起止：</span>{{ stats.currentInternship.startDate }} ~ {{ stats.currentInternship.endDate }}
          </div>

          <!-- 进度条 -->
          <div class="relative mb-[10px]">
            <!-- 底轨 -->
            <div class="w-full h-[6px] bg-[#e5e7eb] rounded-full" />
            <!-- 填充 -->
            <div class="absolute top-0 left-0 h-[6px] bg-[#2563eb] rounded-full"
              style="width: 75%" />
            <!-- 当前节点圆点 -->
            <div class="absolute top-[-4px] w-[14px] h-[14px] rounded-full bg-[#2563eb] border-[2px] border-white shadow-[0_0_0_2px_#2563eb]"
              style="left: calc(75% - 7px)" />
          </div>

          <!-- 阶段标签 -->
          <div class="flex justify-between text-[12px] text-[#757575]">
            <span
              v-for="(stage, i) in stats.progressStages"
              :key="stage.label"
              :class="stage.current ? 'text-[#2563eb] font-medium' : stage.done ? 'text-[#16a34a]' : ''"
            >{{ stage.label }}</span>
          </div>

          <!-- 查看详情按钮 -->
          <div class="mt-[20px] text-right">
            <button
              class="text-[#2563eb] text-[13px] font-medium hover:underline cursor-pointer"
              @click="go('/stu/internships/1')"
            >查看详情 →</button>
          </div>
        </div>

        <!-- 待办事项卡片 -->
        <div class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] p-[24px]">
          <h2 class="text-[18px] font-semibold mb-[4px]">待办事项</h2>
          <p class="text-[14px] text-[#757575] mb-[12px]">你有 {{ stats.pendingItems.length }} 项待处理</p>

          <div v-if="stats.pendingItems.length === 0" class="text-[#9ca3af] text-[14px]">暂无待办</div>
          <div
            v-for="item in stats.pendingItems"
            :key="item.id"
            class="flex items-center justify-between py-[10px] px-[12px] mb-[6px] rounded-[8px] bg-[#f9fafb] cursor-pointer hover:bg-[#f3f4f6] transition-colors"
            @click="go(item.link)"
          >
            <div class="min-w-0 flex-1">
              <div class="text-[14px] font-medium truncate">{{ item.text }}</div>
              <div class="text-[12px] text-[#9ca3af] mt-[2px]">截止：{{ item.deadline }}</div>
            </div>
            <span class="text-[12px] text-[#f59e0b] bg-[rgba(245,158,11,0.1)] px-[8px] py-[2px] rounded-[12px] shrink-0 ml-[12px]">{{ item.status }}</span>
          </div>
        </div>
      </div>

      <!-- ===== 双栏区域 下排 ===== -->
      <div class="grid grid-cols-2 gap-[22px]">

        <!-- 最新通知卡片 -->
        <div class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] p-[24px]">
          <h2 class="text-[18px] font-semibold mb-[16px]">最新通知</h2>
          <div v-for="n in stats.recentNotices" :key="n.id"
            class="flex items-center justify-between py-[10px] px-[12px] mb-[6px] rounded-[8px] bg-[#f9fafb] last:mb-0"
          >
            <div class="min-w-0 flex-1">
              <div class="text-[14px] font-medium truncate">{{ n.title }}</div>
              <div class="text-[12px] text-[#9ca3af] mt-[2px]">{{ n.from }}</div>
            </div>
            <span class="text-[12px] text-[#9ca3af] shrink-0 ml-[12px]">{{ n.time }}</span>
          </div>
        </div>

        <!-- 快捷入口卡片 -->
        <div class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] p-[24px]">
          <h2 class="text-[18px] font-semibold mb-[30px]">快捷入口</h2>
          <div class="grid grid-cols-4 gap-[16px]">
            <div
              v-for="action in stats.quickActions"
              :key="action.label"
              class="flex flex-col items-center justify-center bg-[#f9fafb] rounded-[12px] p-[16px_8px] cursor-pointer hover:bg-[#eff6ff] hover:text-[#2563eb] transition-colors min-h-[90px]"
              @click="go(action.link)"
            >
              <span class="text-[24px] mb-[8px]">{{ action.icon }}</span>
              <span class="text-[13px] font-medium">{{ action.label }}</span>
            </div>
          </div>
        </div>
      </div>

    </div>
  </LayoutBackend>
</template>
