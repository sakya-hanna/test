<!--
页面编号：COU-STU-01
页面名称：学生列表
路由：/counselor/student-list
说明：展示辅导员所带全部学生的基本信息，与"学生实习跟踪"（实习进度）区分
-->
<script setup>
import { ref, computed } from 'vue'

const searchQuery = ref('')
const selectedStudent = ref(null)
const showDetail = ref(false)

const students = ref([
  { id:1, name:'李明', sid:'2024030112', cls:'软件2201', gender:'男', phone:'138-0001-1234', parentName:'李父', parentPhone:'139-0001-5678', dorm:'B3-512', grade:'2022级' },
  { id:2, name:'王芳', sid:'2024030115', cls:'软件2202', gender:'女', phone:'138-0002-1234', parentName:'王母', parentPhone:'139-0002-5678', dorm:'A2-308', grade:'2022级' },
  { id:3, name:'赵强', sid:'2024030120', cls:'计算机2201', gender:'男', phone:'138-0003-1234', parentName:'赵父', parentPhone:'139-0003-5678', dorm:'B3-621', grade:'2022级' },
  { id:4, name:'陈雪', sid:'2024030125', cls:'计算机2202', gender:'女', phone:'138-0004-1234', parentName:'陈母', parentPhone:'139-0004-5678', dorm:'A2-412', grade:'2022级' },
  { id:5, name:'刘洋', sid:'2024030130', cls:'软件2201', gender:'男', phone:'138-0005-1234', parentName:'刘父', parentPhone:'139-0005-5678', dorm:'B3-518', grade:'2022级' },
  { id:6, name:'周婷', sid:'2024030135', cls:'计算机2201', gender:'女', phone:'138-0006-1234', parentName:'周父', parentPhone:'139-0006-5678', dorm:'A2-501', grade:'2022级' },
  { id:7, name:'吴磊', sid:'2024030140', cls:'软件2202', gender:'男', phone:'138-0007-1234', parentName:'吴母', parentPhone:'139-0007-5678', dorm:'B3-705', grade:'2022级' },
  { id:8, name:'郑欣', sid:'2024030145', cls:'计算机2201', gender:'女', phone:'138-0008-1234', parentName:'郑父', parentPhone:'139-0008-5678', dorm:'A2-216', grade:'2022级' },
])

const stats = computed(() => ({
  total: students.value.length,
  inInternship: 6,
  noInternship: students.value.length - 6,
  classes: [...new Set(students.value.map(s => s.cls))].length,
}))

const filteredStudents = computed(() => {
  if (!searchQuery.value) return students.value
  return students.value.filter(s =>
    s.name.includes(searchQuery.value) || s.sid.includes(searchQuery.value) || s.cls.includes(searchQuery.value)
  )
})

function openDetail(student) {
  selectedStudent.value = student
  showDetail.value = true
}
</script>

<template>
  <div class="flex min-h-screen">
    <!-- 侧边栏 — 与 Dashboard 完全一致 -->
    <aside class="w-[232px] min-h-screen flex-shrink-0 text-white flex flex-col" style="background:#00203D;">
      <div class="px-5 py-5 border-b border-white/10 text-lg font-semibold">实习管理系统</div>
      <nav class="flex-1 py-3 px-3 space-y-0.5">
        <router-link to="/counselor/dashboard" class="flex items-center px-4 py-3 rounded-lg text-[16px] text-white/80 hover:bg-white/10">指导工作台</router-link>
        <div>
          <router-link to="/counselor/student-list" class="flex items-center px-4 py-3 rounded-lg text-[16px] text-white/80 hover:bg-white/10">我的学生</router-link>
          <div class="ml-6 mt-0.5 space-y-0.5">
            <router-link to="/counselor/student-list" class="block text-[16px] px-2 py-1 rounded" style="color:#C7D2FE;">学生列表</router-link>
            <router-link to="/counselor/students" class="block text-[16px] text-white/80 px-2 py-1 rounded hover:bg-white/10">学生实习跟踪</router-link>
            <router-link to="/counselor/alerts" class="block text-[16px] text-white/80 px-2 py-1 rounded hover:bg-white/10">安全提醒沟通</router-link>
            <router-link to="/counselor/parent-notice" class="block text-[16px] text-white/80 px-2 py-1 rounded hover:bg-white/10">家长知情确认</router-link>
          </div>
        </div>
        <router-link to="/counselor/process-feedback" class="flex items-center px-4 py-3 rounded-lg text-[16px] text-white/80 hover:bg-white/10">过程反馈</router-link>
        <router-link to="/counselor/evaluations" class="flex items-center px-4 py-3 rounded-lg text-[16px] text-white/80 hover:bg-white/10">学生评价</router-link>
        <router-link to="/counselor/notifications" class="flex items-center px-4 py-3 rounded-lg text-[16px] text-white/80 hover:bg-white/10">通知公告</router-link>
      </nav>
    </aside>

    <!-- 右侧主体 -->
    <div class="flex-1 flex flex-col" style="background:#F5F7FB;">
      <header class="h-[72px] bg-white flex items-center justify-between px-6 shadow-sm flex-shrink-0">
        <span class="text-[14px]" style="color:#757575;">我的学生 / 学生列表</span>
        <div class="flex items-center gap-4">
          <div class="flex items-center gap-2">
            <div class="w-10 h-10 rounded-full flex items-center justify-center text-xs font-medium" style="background:#C8C8C8;">孙</div>
            <div>
              <div class="text-[14px] font-medium text-gray-900">孙辅导员</div>
              <div class="text-[12px]" style="color:#2563EB;">辅导员</div>
            </div>
          </div>
          <router-link to="/login" class="text-[14px] font-medium" style="color:#FF383C;">登出</router-link>
        </div>
      </header>

      <main class="flex-1 overflow-auto px-10 py-6">
        <!-- 页面标题 -->
        <div class="mb-5">
          <p class="text-[14px]" style="color:#757575;">我的学生 / 学生列表</p>
          <h2 class="text-[32px] font-bold text-gray-900 mt-1">学生列表</h2>
          <p class="text-[16px] mt-1" style="color:#757575;">查看和管理所带学生的基本信息</p>
        </div>

        <!-- 统计卡片 -->
        <div class="grid grid-cols-4 gap-4 mb-5">
          <div class="bg-white rounded-[12px] p-5">
            <p class="text-[13px] mb-2" style="color:#6B7280;">全部学生</p>
            <p class="text-[28px] font-bold" style="color:#2563EB;">{{ stats.total }}</p>
          </div>
          <div class="bg-white rounded-[12px] p-5">
            <p class="text-[13px] mb-2" style="color:#6B7280;">实习中</p>
            <p class="text-[28px] font-bold" style="color:#16A34A;">{{ stats.inInternship }}</p>
          </div>
          <div class="bg-white rounded-[12px] p-5">
            <p class="text-[13px] mb-2" style="color:#6B7280;">待安排</p>
            <p class="text-[28px] font-bold" style="color:#D97706;">{{ stats.noInternship }}</p>
          </div>
          <div class="bg-white rounded-[12px] p-5">
            <p class="text-[13px] mb-2" style="color:#6B7280;">覆盖班级</p>
            <p class="text-[28px] font-bold" style="color:#16A34A;">{{ stats.classes }}</p>
          </div>
        </div>

        <!-- 搜索 -->
        <div class="flex items-center justify-between mb-5">
          <span class="text-[14px] font-medium text-gray-900">共 {{ filteredStudents.length }} 名学生</span>
          <div class="w-[260px]">
            <input v-model="searchQuery" type="text" placeholder="搜索姓名、学号或班级..."
              class="w-full h-[36px] rounded-[8px] border px-3 text-[14px] outline-none"
              style="border-color:rgba(0,0,0,0.16); color:#9CA3AF; background:white;" />
          </div>
        </div>

        <!-- 数据表格 -->
        <div class="bg-white rounded-[8px] shadow-sm mb-4 overflow-hidden">
          <div class="h-[44px] flex items-center px-4" style="background:#F3F4F6;">
            <div class="text-[12px] font-semibold flex-shrink-0" style="color:#374151; width:80px;">学生姓名</div>
            <div class="text-[12px] font-semibold flex-shrink-0" style="color:#374151; width:100px;">学号</div>
            <div class="text-[12px] font-semibold flex-shrink-0" style="color:#374151; width:100px;">班级</div>
            <div class="text-[12px] font-semibold flex-shrink-0" style="color:#374151; width:60px;">性别</div>
            <div class="text-[12px] font-semibold flex-shrink-0" style="color:#374151; width:130px;">手机号</div>
            <div class="text-[12px] font-semibold flex-shrink-0" style="color:#374151; width:100px;">家长姓名</div>
            <div class="text-[12px] font-semibold flex-shrink-0" style="color:#374151; width:140px;">家长联系方式</div>
            <div class="text-[12px] font-semibold flex-shrink-0" style="color:#374151; width:80px;">宿舍</div>
            <div class="text-[12px] font-semibold flex-shrink-0" style="color:#374151; width:60px;">操作</div>
          </div>
          <div v-for="(s, i) in filteredStudents" :key="s.id">
            <div class="h-[52px] flex items-center px-4 hover:bg-[#F9FAFB] transition-colors">
              <div class="text-[12px] flex-shrink-0" style="color:#374151; width:80px;">{{ s.name }}</div>
              <div class="text-[12px] flex-shrink-0" style="color:#374151; width:100px;">{{ s.sid }}</div>
              <div class="text-[12px] flex-shrink-0" style="color:#374151; width:100px;">{{ s.cls }}</div>
              <div class="text-[12px] flex-shrink-0" style="color:#374151; width:60px;">{{ s.gender }}</div>
              <div class="text-[12px] flex-shrink-0" style="color:#374151; width:130px;">{{ s.phone }}</div>
              <div class="text-[12px] flex-shrink-0" style="color:#374151; width:100px;">{{ s.parentName }}</div>
              <div class="text-[12px] flex-shrink-0" style="color:#374151; width:140px;">{{ s.parentPhone }}</div>
              <div class="text-[12px] flex-shrink-0" style="color:#374151; width:80px;">{{ s.dorm }}</div>
              <div class="flex-shrink-0" style="width:60px;">
                <span class="text-[13px] font-medium cursor-pointer hover:underline" style="color:#2563EB;" @click="openDetail(s)">详情</span>
              </div>
            </div>
            <div v-if="i < filteredStudents.length - 1" class="h-px mx-4" style="background:#E5E7EB;"></div>
          </div>
          <div v-if="filteredStudents.length === 0" class="h-[120px] flex items-center justify-center text-[14px]" style="color:#9CA3AF;">暂无匹配的学生数据</div>
        </div>
      </main>
    </div>

    <!-- 学生详情弹窗 -->
    <div v-if="showDetail" class="fixed inset-0 z-50 flex items-center justify-center" style="background:rgba(0,0,0,0.45);" @click.self="showDetail = false">
      <div class="bg-white rounded-[12px] overflow-hidden" style="width:520px; max-height:80vh; overflow-y:auto;">
        <div class="p-[20px_24px] border-b border-[#E5E7EB] flex items-center justify-between">
          <span class="text-[18px] font-semibold text-[#111827]">学生信息 - {{ selectedStudent?.name }}</span>
          <button class="w-[28px] h-[28px] flex items-center justify-center rounded-[6px] text-[18px] text-[#6B7280] hover:bg-[#F3F4F6] border-none bg-none cursor-pointer" @click="showDetail = false">✕</button>
        </div>
        <div class="p-[24px]">
          <div class="grid grid-cols-2 gap-y-3 gap-x-5">
            <div class="flex gap-2 text-[13px]"><span style="color:#6B7280;">姓名：</span><span style="color:#111827;">{{ selectedStudent?.name }}</span></div>
            <div class="flex gap-2 text-[13px]"><span style="color:#6B7280;">学号：</span><span style="color:#111827;">{{ selectedStudent?.sid }}</span></div>
            <div class="flex gap-2 text-[13px]"><span style="color:#6B7280;">班级：</span><span style="color:#111827;">{{ selectedStudent?.cls }}</span></div>
            <div class="flex gap-2 text-[13px]"><span style="color:#6B7280;">性别：</span><span style="color:#111827;">{{ selectedStudent?.gender }}</span></div>
            <div class="flex gap-2 text-[13px]"><span style="color:#6B7280;">年级：</span><span style="color:#111827;">{{ selectedStudent?.grade }}</span></div>
            <div class="flex gap-2 text-[13px]"><span style="color:#6B7280;">手机号：</span><span style="color:#111827;">{{ selectedStudent?.phone }}</span></div>
            <div class="flex gap-2 text-[13px]"><span style="color:#6B7280;">家长姓名：</span><span style="color:#111827;">{{ selectedStudent?.parentName }}</span></div>
            <div class="flex gap-2 text-[13px]"><span style="color:#6B7280;">家长联系方式：</span><span style="color:#111827;">{{ selectedStudent?.parentPhone }}</span></div>
            <div class="flex gap-2 text-[13px]"><span style="color:#6B7280;">宿舍：</span><span style="color:#111827;">{{ selectedStudent?.dorm }}</span></div>
          </div>
        </div>
        <div class="p-[16px_24px] border-t border-[#E5E7EB] flex justify-end">
          <button class="h-[36px] px-[18px] rounded-[6px] text-[14px] cursor-pointer border border-[#D1D5DB] bg-white text-[#374151] hover:border-[#2563EB] hover:text-[#2563EB] transition-colors" @click="showDetail = false">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>
