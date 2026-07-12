<!--
页面编号：STU-INTRN-01-D01
页面名称：实习详情页
路由：/stu/internships/:internshipId
Figma Node：182:15
当前阶段：阶段一-原型还原
-->
<script setup>
import { ref, computed } from 'vue'
import LayoutBackend from '../../layouts/LayoutBackend.vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const internshipId = computed(() => route.params.internshipId)

// 模拟详情数据（按 ID 可区分）
const detail = {
  id: internshipId.value,
  type: '毕业实习',
  company: '华为技术有限公司',
  position: '软件开发工程师',
  status: '进行中',
  applyDate: '2026-05-20',
  startDate: '2026-06-01',
  endDate: '2026-08-31',
  address: '深圳市龙岗区坂田华为基地',
  contactPerson: '李明',
  contactPhone: '138****6789',
  instructor: '王老师（校内）',
  enterpriseMentor: '张工（企业导师）',
}

// 基本信息字段
const infoFields = [
  { label: '实习类型', value: detail.type },
  { label: '实习单位', value: detail.company },
  { label: '实习岗位', value: detail.position },
  { label: '单位地址', value: detail.address },
  { label: '起止时间', value: `${detail.startDate} ~ ${detail.endDate}` },
  { label: '联系人', value: `${detail.contactPerson}  ${detail.contactPhone}` },
  { label: '校内指导老师', value: detail.instructor },
  { label: '企业导师', value: detail.enterpriseMentor },
]

// 时间轴阶段
const timeline = [
  { name: '提交实习申请', time: '2026-05-20', status: '已完成', done: true },
  { name: '指导老师审核通过', time: '2026-05-22', status: '已完成', done: true },
  { name: '学院复核通过', time: '2026-05-24', status: '已完成', done: true },
  { name: '学校备案确认', time: '2026-05-25', status: '已完成', done: true },
  { name: '实习进行中', time: '2026-06-01 起', status: '进行中', done: true, current: true },
  { name: '提交实习成果', time: '截止 2026-08-31', status: '待完成', done: false },
  { name: '企业导师评价', time: '预计 2026-08-25', status: '待完成', done: false },
  { name: '校内老师评定', time: '预计 2026-09-05', status: '待完成', done: false },
]

// Tab 与表格
const activeTab = ref('weekly')
const tabs = [
  { key: 'weekly', label: '周报' },
  { key: 'monthly', label: '月报' },
  { key: 'material', label: '材料' },
  { key: 'evaluation', label: '评价' },
]

const weeklyReports = [
  { week: '第1周', title: '实习第1周工作周报', time: '2026-06-07', review: '已评阅', reviewer: '王老师', status: '已通过' },
  { week: '第2周', title: '实习第2周工作周报', time: '2026-06-14', review: '已评阅', reviewer: '王老师', status: '已通过' },
  { week: '第3周', title: '实习第3周工作周报', time: '2026-06-21', review: '待评阅', reviewer: '—', status: '待审核' },
  { week: '第4周', title: '—', time: '—', review: '—', reviewer: '—', status: '待提交' },
]

const statusMap = {
  '已通过': { color: '#16A34A', bg: 'rgba(20,141,61,0.25)' },
  '待审核': { color: '#F59E0B', bg: 'rgba(245,158,11,0.1)' },
  '待提交': { color: '#9CA3AF', bg: 'rgba(0,0,0,0.05)' },
  '已完成': { color: '#16A34A', bg: 'rgba(20,141,61,0.25)' },
  '进行中': { color: '#2563EB', bg: 'rgba(37,99,235,0.1)' },
  '待完成': { color: '#9CA3AF', bg: 'rgba(0,0,0,0.05)' },
}

function goBack() {
  router.push('/stu/internships')
}

function goReport(type) {
  // TODO: 跳转到周报/月报/材料提交页
  router.push(`/stu/reports/${type}/new`).catch(() => {})
}

function goViewWeek(week) {
  // TODO: 查看周报详情
  alert(`查看${week}周报详情（待实现）`)
}
</script>

<template>
  <LayoutBackend>
    <div class="p-[30px_40px]">

      <!-- ===== 页面标题 ===== -->
      <div class="flex items-start justify-between mb-[24px]">
        <div>
          <h1 class="text-[32px] font-bold text-black mb-[4px]">实习详情</h1>
          <p class="text-[16px] text-[#757575]">{{ detail.company }} · {{ detail.position }}</p>
        </div>
        <button
          class="bg-white text-[#6b7280] text-[14px] font-medium px-[20px] py-[8px] rounded-[24px] border border-[rgba(0,0,0,0.25)] hover:bg-[#f9fafb] transition-colors cursor-pointer"
          @click="goBack"
        >← 返回列表</button>
      </div>

      <!-- ===== 双栏区域：基本信息 + 时间轴 ===== -->
      <div class="grid grid-cols-[380px_1fr] gap-[22px] mb-[22px]">

        <!-- 左栏：基本信息 -->
        <div class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] p-[24px]">
          <div class="flex items-center justify-between mb-[20px]">
            <h2 class="text-[18px] font-semibold">基本信息</h2>
            <span
              class="inline-block px-[10px] py-[2px] rounded-[12px] text-[12px] font-medium"
              :style="{
                color: (statusMap[detail.status] || {}).color || '#6b7280',
                background: (statusMap[detail.status] || {}).bg || 'rgba(0,0,0,0.05)',
              }"
            >{{ detail.status }}</span>
          </div>

          <div v-for="field in infoFields" :key="field.label" class="mb-[16px] last:mb-0">
            <div class="text-[12px] text-[#9ca3af] mb-[2px]">{{ field.label }}</div>
            <div class="text-[14px] text-[#1f2937]">{{ field.value }}</div>
          </div>
        </div>

        <!-- 右栏：过程记录时间轴 -->
        <div class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] p-[24px]">
          <h2 class="text-[18px] font-semibold mb-[20px]">过程记录</h2>

          <div class="relative pl-[32px]">
            <!-- 时间轴竖线 -->
            <div class="absolute left-[7px] top-[8px] bottom-[8px] w-[2px] bg-[#e5e7eb]" />

            <div
              v-for="(node, i) in timeline"
              :key="i"
              class="relative mb-[24px] last:mb-0"
            >
              <!-- 节点圆点 -->
              <div
                class="absolute left-[-25px] top-[4px] w-[16px] h-[16px] rounded-full z-10"
                :class="node.done ? 'bg-[#2563eb]' : 'bg-[#d1d5db]'"
              />
              <!-- 当前节点光环 -->
              <div
                v-if="node.current"
                class="absolute left-[-29px] top-0 w-[24px] h-[24px] rounded-full border-[3px] border-[rgba(37,99,235,0.3)] z-0"
              />

              <!-- 内容 -->
              <div class="flex items-start justify-between">
                <div>
                  <div class="text-[14px] font-medium" :class="node.current ? 'text-[#2563eb]' : 'text-[#1f2937]'">
                    {{ node.name }}
                  </div>
                  <div class="text-[12px] text-[#9ca3af] mt-[2px]">{{ node.time }}</div>
                </div>
                <span
                  class="inline-block px-[8px] py-[1px] rounded-[12px] text-[11px] font-medium shrink-0"
                  :style="{
                    color: (statusMap[node.status] || {}).color || '#6b7280',
                    background: (statusMap[node.status] || {}).bg || 'rgba(0,0,0,0.05)',
                  }"
                >{{ node.status }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- ===== 过程记录详情（Tab + 表格） ===== -->
      <div class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] overflow-hidden">
        <!-- Tab 栏 -->
        <div class="flex items-center gap-[24px] px-[24px] pt-[20px] border-b border-[#e5e7eb]">
          <button
            v-for="tab in tabs"
            :key="tab.key"
            class="text-[15px] pb-[12px] border-b-[3px] transition-colors cursor-pointer"
            :class="activeTab === tab.key
              ? 'text-[#2563eb] border-[#2563eb] font-medium'
              : 'text-[#757575] border-transparent hover:text-[#374151]'"
            @click="activeTab = tab.key"
          >{{ tab.label }}</button>
        </div>

        <!-- 表格 -->
        <div>
          <!-- 表头 -->
          <div class="grid grid-cols-[100px_260px_160px_180px_100px_80px] gap-[10px] px-[24px] py-[14px] bg-[#f9fafb] text-[13px] font-medium text-[#6b7280] border-b border-[#e5e7eb]">
            <div>周次</div>
            <div>周报标题</div>
            <div>提交时间</div>
            <div>指导老师评阅</div>
            <div>状态</div>
            <div>操作</div>
          </div>

          <!-- 数据行 -->
          <div
            v-for="(row, idx) in weeklyReports"
            :key="idx"
            class="grid grid-cols-[100px_260px_160px_180px_100px_80px] gap-[10px] px-[24px] py-[16px] text-[14px] items-center hover:bg-[#f9fafb] transition-colors"
            :class="{ 'border-t border-[#f3f4f6]': idx > 0 }"
          >
            <div class="text-[#374151]">{{ row.week }}</div>
            <div class="font-medium truncate" :class="row.title === '—' ? 'text-[#9ca3af]' : ''">{{ row.title }}</div>
            <div class="text-[#757575]">{{ row.time }}</div>
            <div class="text-[12px]">
              <span v-if="row.review === '已评阅'" class="text-[#16a34a]">{{ row.reviewer }} 已评阅</span>
              <span v-else class="text-[#9ca3af]">{{ row.review }}</span>
            </div>
            <div>
              <span
                class="inline-block px-[8px] py-[1px] rounded-[12px] text-[11px] font-medium"
                :style="{
                  color: (statusMap[row.status] || {}).color || '#6b7280',
                  background: (statusMap[row.status] || {}).bg || 'rgba(0,0,0,0.05)',
                }"
              >{{ row.status }}</span>
            </div>
            <div>
              <button
                v-if="row.status !== '待提交'"
                class="text-[#2563eb] text-[13px] font-medium hover:underline cursor-pointer"
                @click="goViewWeek(row.week)"
              >查看</button>
              <span v-else class="text-[#d1d5db] text-[13px]">—</span>
            </div>
          </div>
        </div>
      </div>

    </div>
  </LayoutBackend>
</template>
