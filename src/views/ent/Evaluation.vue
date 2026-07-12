<!-- Figma: 230:16 - ENT-EVAL-01 - 企业导师评价管理 1440x1140 -->
<template>
  <div class="flex min-h-screen">
    <!-- 侧边栏 232px bg=#00203D -->
    <CompanySidebar />

    <div class="flex-1 flex flex-col" style="background:#F5F7FB;">
      <!-- 顶栏 -->
      <header class="h-[72px] bg-white flex items-center justify-between px-6 shadow-sm flex-shrink-0">
        <span class="text-[14px]" style="color:#757575;">企业管理 / 学生评价</span>
        <div class="flex items-center gap-4">
          <div>
            <div class="text-[14px] font-medium text-right" style="color:#000;">陈志远</div>
            <div class="text-[12px]" style="color:#16A34A;">企业指导老师</div>
          </div>
          <router-link to="/login" class="text-[14px] font-medium" style="color:#FF383C;">登出</router-link>
        </div>
      </header>

      <main class="flex-1 overflow-auto p-6">
        <div class="mb-6">
          <span class="text-[14px]" style="color:#757575;">企业管理 / 学生评价</span>
          <h2 class="text-[32px] font-bold text-gray-900 mt-1">企业导师评价管理</h2>
          <p class="text-[16px] mt-1" style="color:#757575;">填写学生在岗工作表现评价与评分，提交后计入学生实习成绩</p>
        </div>

        <!-- 学生基本信息卡片 -->
        <div class="bg-white rounded-2xl p-6 shadow-sm mb-6">
          <h3 class="text-[16px] font-bold mb-4" style="color:#000;">学生基本信息</h3>
          <div class="grid grid-cols-4 gap-y-3 gap-x-6 text-[14px]">
            <div><span style="color:#757575;">姓名</span><span class="ml-4" style="color:#111827;">王思远</span></div>
            <div><span style="color:#757575;">学号</span><span class="ml-4" style="color:#111827;">2024010101</span></div>
            <div><span style="color:#757575;">学校</span><span class="ml-4" style="color:#111827;">无锡学院</span></div>
            <div><span style="color:#757575;">专业</span><span class="ml-4" style="color:#111827;">软件工程</span></div>
            <div><span style="color:#757575;">实习岗位</span><span class="ml-4" style="color:#111827;">前端开发工程师</span></div>
            <div><span style="color:#757575;">实习类型</span><span class="ml-4" style="color:#111827;">毕业实习</span></div>
            <div><span style="color:#757575;">实习时间</span><span class="ml-4" style="color:#111827;">2026-03-01 至 2026-08-31</span></div>
            <div><span style="color:#757575;">企业导师</span><span class="ml-4" style="color:#111827;">陈志远</span></div>
          </div>
        </div>

        <!-- 维度评分 -->
        <div class="bg-white rounded-2xl p-6 shadow-sm mb-6">
          <div class="flex justify-between items-center mb-4">
            <h3 class="text-[16px] font-bold" style="color:#000;">维度评分</h3>
            <span class="text-[13px]" style="color:#9CA3AF;">各项满分 100 分</span>
          </div>
          <div class="grid grid-cols-2 gap-x-12 gap-y-4 mb-4">
            <div class="flex items-center gap-3">
              <span class="text-[14px] w-20" style="color:#374151;">工作态度</span>
              <input v-model="scores.attitude" class="w-[76px] h-[32px] text-center text-[15px] font-medium rounded-lg border" style="border-color:#D1D5DB; color:#2563EB;" />
              <span class="text-[13px]" style="color:#6B7280;">分</span>
            </div>
            <div class="flex items-center gap-3">
              <span class="text-[14px] w-20" style="color:#374151;">专业能力</span>
              <input v-model="scores.skill" class="w-[76px] h-[32px] text-center text-[15px] font-medium rounded-lg border" style="border-color:#D1D5DB; color:#2563EB;" />
              <span class="text-[13px]" style="color:#6B7280;">分</span>
            </div>
            <div class="flex items-center gap-3">
              <span class="text-[14px] w-20" style="color:#374151;">团队协作</span>
              <input v-model="scores.teamwork" class="w-[76px] h-[32px] text-center text-[15px] font-medium rounded-lg border" style="border-color:#D1D5DB; color:#2563EB;" />
              <span class="text-[13px]" style="color:#6B7280;">分</span>
            </div>
            <div class="flex items-center gap-3">
              <span class="text-[14px] w-20" style="color:#374151;">学习能力</span>
              <input v-model="scores.learning" class="w-[76px] h-[32px] text-center text-[15px] font-medium rounded-lg border" style="border-color:#D1D5DB; color:#2563EB;" />
              <span class="text-[13px]" style="color:#6B7280;">分</span>
            </div>
            <div class="flex items-center gap-3">
              <span class="text-[14px] w-20" style="color:#374151;">沟通表达</span>
              <input v-model="scores.expression" class="w-[76px] h-[32px] text-center text-[15px] font-medium rounded-lg border" style="border-color:#D1D5DB; color:#2563EB;" />
              <span class="text-[13px]" style="color:#6B7280;">分</span>
            </div>
          </div>
          <!-- 综合评分 -->
          <div class="rounded-lg px-4 py-3 flex justify-between items-center" style="background:rgba(22,163,74,0.1);">
            <span class="text-[13px] font-medium" style="color:#16A34A;">综合评分（各项平均值，系统自动计算）</span>
            <span class="text-[22px] font-bold" style="color:#16A34A;">{{ computedAvg }}<span class="text-[14px] font-normal"> 分</span></span>
          </div>
        </div>

        <!-- 文字评价 -->
        <div class="bg-white rounded-2xl p-6 shadow-sm mb-6">
          <h3 class="text-[16px] font-bold mb-4" style="color:#000;">文字评价</h3>
          <div class="space-y-4">
            <div>
              <label class="block text-[13px] font-medium mb-2" style="color:#374151;">学生工作内容</label>
              <textarea rows="4" class="w-full px-3 py-2 rounded-lg border text-[13px] resize-none focus:outline-none" style="border-color:#E5E7EB; background:#FAFAFA; color:#374151;" placeholder="请描述学生工作内容">负责公司数据可视化产品前端组件库的开发与维护，参与核心报表模块的迭代与缺陷修复；独立完成大屏图表渲染性能优化，封装可复用图表组件 12 个，并编写接口与技术文档，按时高质量交付各期需求。</textarea>
            </div>
            <div>
              <label class="block text-[13px] font-medium mb-2" style="color:#374151;">项目参与情况</label>
              <textarea rows="4" class="w-full px-3 py-2 rounded-lg border text-[13px] resize-none focus:outline-none" style="border-color:#E5E7EB; background:#FAFAFA; color:#374151;" placeholder="请描述项目参与情况">参与「企业经营数据大屏」项目，承担图表交互层与数据联动的开发；与后端工程师协作完成接口定义、对接与联调，主动梳理数据口径不一致问题并输出校验规则，推动项目提前 3 天上线，获得业务方好评与内部复盘表扬。</textarea>
            </div>
            <div>
              <label class="block text-[13px] font-medium mb-2" style="color:#374151;">工作表现评语</label>
              <textarea rows="3" class="w-full px-3 py-2 rounded-lg border text-[13px] resize-none focus:outline-none" style="border-color:#E5E7EB; background:#FAFAFA;" placeholder="请输入对学生工作态度、专业能力、团队协作等方面的评语…"></textarea>
            </div>
            <div>
              <label class="block text-[13px] font-medium mb-2" style="color:#374151;">综合评语</label>
              <textarea rows="2" class="w-full px-3 py-2 rounded-lg border text-[13px] resize-none focus:outline-none" style="border-color:#E5E7EB; background:#FAFAFA;" placeholder="请输入综合评语…"></textarea>
            </div>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="flex justify-end gap-4">
          <button class="w-[110px] h-[42px] rounded-lg text-[14px] font-medium" style="border:1px solid #D1D5DB; color:#6B7280; background:#fff;">取消</button>
          <button class="w-[120px] h-[42px] rounded-lg text-[14px] font-medium" style="border:1px solid #2563EB; color:#2563EB; background:#fff;">暂存</button>
          <button class="w-[150px] h-[42px] rounded-lg text-[14px] font-medium text-white" style="background:#2563EB;">提交评价</button>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import CompanySidebar from '../../components/CompanySidebar.vue'
import { reactive, computed } from 'vue'
function tip() { alert('原型展示，目标待确认') }
const scores = reactive({ attitude: 90, skill: 88, teamwork: 85, learning: 92, expression: 86 })
const computedAvg = computed(() => {
  const vals = Object.values(scores)
  return Math.round(vals.reduce((a,b) => a + Number(b), 0) / vals.length)
})
</script>
