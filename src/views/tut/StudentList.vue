<!--
页面编号：TUT-INTRN-01
页面名称：指导学生列表
路由：/teacher/students
Figma Node：106:15
当前阶段：阶段一-原型还原
-->
<script setup>
import { ref, computed } from 'vue'
import LayoutTeacher from '../../layouts/LayoutTeacher.vue'
import { tutStudentList, tutStudentStatusMap, alertLevelMap } from '../../mock/tut.js'

// 筛选
const activeTab = ref('')
const searchQuery = ref('')

const filteredList = computed(() => {
  let list = tutStudentList
  if (activeTab.value) {
    list = list.filter(item => item.status === activeTab.value)
  }
  if (searchQuery.value.trim()) {
    const q = searchQuery.value.trim().toLowerCase()
    list = list.filter(item =>
      item.studentName.toLowerCase().includes(q) ||
      item.company.toLowerCase().includes(q) ||
      item.studentNo.includes(q)
    )
  }
  return list
})

// 详情弹窗
const dialogVisible = ref(false)
const currentStudent = ref(null)

function openDetail(student) {
  currentStudent.value = student
  dialogVisible.value = true
}

function closeDialog() {
  dialogVisible.value = false
  currentStudent.value = null
}

const stats = computed(() => [
  { label: '全部学生', value: tutStudentList.length, color: '#2563EB' },
  { label: '进行中', value: tutStudentList.filter(i => i.status === '进行中').length, color: '#F59E0B' },
  { label: '已完成', value: tutStudentList.filter(i => i.status === '已完成').length, color: '#16A34A' },
  { label: '需关注', value: tutStudentList.filter(i => i.alertLevel === 'warning').length, color: '#FF383C' },
])

const statusTabs = [
  { label: '全部', value: '' },
  { label: '进行中', value: '进行中' },
  { label: '已完成', value: '已完成' },
]
</script>

<template>
  <LayoutTeacher>
    <div class="p-[30px_40px]">

      <h1 class="text-[32px] font-bold text-black mb-[4px]">指导学生列表</h1>
      <p class="text-[16px] text-[#757575] mb-[24px]">查看所指导学生的实习进展与里程碑完成情况</p>

      <!-- 统计卡片 -->
      <div class="grid grid-cols-4 gap-[22px] mb-[22px]">
        <div
          v-for="card in stats"
          :key="card.label"
          class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] p-[20px_24px] flex flex-col items-center justify-center min-h-[110px]"
        >
          <span class="text-[14px] text-[#757575] mb-[4px]">{{ card.label }}</span>
          <span class="text-[32px] font-bold" :style="{ color: card.color }">{{ card.value }}</span>
        </div>
      </div>

      <!-- 筛选 + 搜索 -->
      <div class="flex items-center justify-between mb-[16px]">
        <div class="flex items-center gap-[42px]">
          <button
            v-for="tab in statusTabs"
            :key="tab.value"
            class="text-[14px] pb-[8px] border-b-[3px] transition-colors cursor-pointer"
            :class="activeTab === tab.value
              ? 'text-[#2563eb] border-[#2563eb] font-medium'
              : 'text-[#757575] border-transparent hover:text-[#374151]'"
            @click="activeTab = tab.value"
          >{{ tab.label }}</button>
        </div>
        <input
          v-model="searchQuery"
          type="text"
          placeholder="🔍 搜索学生姓名、学号、企业..."
          class="w-[300px] h-[36px] bg-white border border-[rgba(0,0,0,0.16)] rounded-[8px] px-[12px] text-[14px] outline-none focus:border-[#2563eb] placeholder-[#9ca3af]"
        />
      </div>

      <div class="border-t border-[#e5e7eb] mb-0" />

      <!-- 数据表格 -->
      <div class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] overflow-hidden">
        <div class="grid grid-cols-[100px_90px_1fr_100px_80px_80px_80px_80px] gap-[10px] px-[20px] py-[14px] bg-[#f9fafb] text-[13px] font-medium text-[#6b7280] border-b border-[#e5e7eb]">
          <div>学生</div>
          <div>班级</div>
          <div>企业 / 岗位</div>
          <div>起止时间</div>
          <div>周报</div>
          <div>月报</div>
          <div>状态</div>
          <div>操作</div>
        </div>

        <div v-if="filteredList.length === 0" class="text-center py-[60px] text-[#9ca3af] text-[14px]">
          暂无匹配的学生记录
        </div>

        <div
          v-for="(item, idx) in filteredList"
          :key="item.id"
          class="grid grid-cols-[100px_90px_1fr_100px_80px_80px_80px_80px] gap-[10px] px-[20px] py-[16px] text-[14px] items-center hover:bg-[#f9fafb] transition-colors"
          :class="{ 'border-t border-[#f3f4f6]': idx > 0 }"
        >
          <div>
            <div class="font-medium">{{ item.studentName }}</div>
            <div class="text-[12px] text-[#9ca3af]">{{ item.studentNo }}</div>
          </div>
          <div class="text-[#757575]">{{ item.class }}</div>
          <div>
            <div class="font-medium truncate">{{ item.company }}</div>
            <div class="text-[12px] text-[#757575] truncate">{{ item.position }}</div>
          </div>
          <div class="text-[12px] text-[#757575]">
            <div>{{ item.startDate }}</div>
            <div>~{{ item.endDate }}</div>
          </div>
          <div class="text-center">
            <span class="text-[#2563eb] font-medium">{{ item.weeklyReports }}</span>
            <span class="text-[12px] text-[#9ca3af]">篇</span>
          </div>
          <div class="text-center">
            <span class="text-[#7c3aed] font-medium">{{ item.monthlyReports }}</span>
            <span class="text-[12px] text-[#9ca3af]">篇</span>
          </div>
          <div class="flex items-center gap-[6px]">
            <span
              class="inline-block px-[10px] py-[2px] rounded-[12px] text-[12px] font-medium"
              :style="{ color: tutStudentStatusMap[item.status]?.color, background: tutStudentStatusMap[item.status]?.bg }"
            >{{ item.status }}</span>
            <span
              v-if="item.alertLevel !== 'normal'"
              class="w-[6px] h-[6px] rounded-full shrink-0"
              :style="{ background: alertLevelMap[item.alertLevel]?.color }"
              :title="alertLevelMap[item.alertLevel]?.label"
            />
          </div>
          <div>
            <button
              class="text-[#2563eb] text-[13px] font-medium hover:underline cursor-pointer"
              @click="openDetail(item)"
            >查看</button>
          </div>
        </div>
      </div>

      <!-- 学生详情弹窗（含里程碑时间轴） -->
      <Teleport to="body">
        <div v-if="dialogVisible" class="fixed inset-0 z-50 flex items-center justify-center">
          <div class="absolute inset-0 bg-black/40" @click="closeDialog" />
          <div class="relative bg-white rounded-[15px] w-[640px] max-h-[85vh] overflow-auto shadow-xl p-[32px]">
            <h2 class="text-[20px] font-bold mb-[20px]">学生实习详情</h2>

            <template v-if="currentStudent">
              <!-- 基本信息 -->
              <div class="bg-[#f9fafb] rounded-[8px] p-[20px] mb-[24px] grid grid-cols-2 gap-[12px] text-[14px]">
                <div><span class="text-[#757575]">姓名：</span><span class="font-medium">{{ currentStudent.studentName }}</span></div>
                <div><span class="text-[#757575]">学号：</span>{{ currentStudent.studentNo }}</div>
                <div><span class="text-[#757575]">班级：</span>{{ currentStudent.class }}</div>
                <div><span class="text-[#757575]">实习类型：</span>{{ currentStudent.type }}</div>
                <div><span class="text-[#757575]">企业：</span>{{ currentStudent.company }}</div>
                <div><span class="text-[#757575]">岗位：</span>{{ currentStudent.position }}</div>
                <div><span class="text-[#757575]">周报：</span><span class="font-medium text-[#2563eb]">{{ currentStudent.weeklyReports }}篇</span></div>
                <div><span class="text-[#757575]">月报：</span><span class="font-medium text-[#7c3aed]">{{ currentStudent.monthlyReports }}篇</span></div>
                <div class="col-span-2 flex items-center gap-[8px]">
                  <span class="text-[#757575]">状态：</span>
                  <span
                    class="inline-block px-[10px] py-[2px] rounded-[12px] text-[12px] font-medium"
                    :style="{ color: tutStudentStatusMap[currentStudent.status]?.color, background: tutStudentStatusMap[currentStudent.status]?.bg }"
                  >{{ currentStudent.status }}</span>
                  <span
                    v-if="currentStudent.alertLevel !== 'normal'"
                    class="inline-block px-[8px] py-[1px] rounded-[12px] text-[12px] font-medium"
                    :style="{ color: alertLevelMap[currentStudent.alertLevel]?.color, background: alertLevelMap[currentStudent.alertLevel]?.bg }"
                  >{{ alertLevelMap[currentStudent.alertLevel]?.label }}</span>
                </div>
              </div>

              <!-- 里程碑时间轴 -->
              <div>
                <h3 class="text-[16px] font-semibold mb-[16px]">实习里程碑</h3>
                <div class="relative pl-[24px]">
                  <!-- 竖线 -->
                  <div class="absolute left-[7px] top-[8px] bottom-[8px] w-[2px] bg-[#e5e7eb]" />

                  <div
                    v-for="(ms, mi) in currentStudent.milestones"
                    :key="ms.label"
                    class="relative mb-[16px] last:mb-0"
                  >
                    <!-- 节点圆点 -->
                    <div
                      class="absolute left-[-21px] top-[4px] w-[12px] h-[12px] rounded-full border-[2px] border-white"
                      :class="ms.done
                        ? 'bg-[#16a34a] shadow-[0_0_0_2px_#16a34a]'
                        : ms.current
                          ? 'bg-[#2563eb] shadow-[0_0_0_2px_#2563eb]'
                          : 'bg-[#d1d5db] shadow-[0_0_0_2px_#d1d5db]'"
                    />
                    <!-- 内容 -->
                    <div
                      class="pb-[4px]"
                      :class="ms.done ? '' : ms.current ? '' : 'opacity-50'"
                    >
                      <div class="text-[14px] font-medium" :class="ms.current ? 'text-[#2563eb]' : ''">
                        {{ ms.label }}
                        <span v-if="ms.current" class="text-[12px] text-[#2563eb] ml-[4px]">● 当前</span>
                      </div>
                      <div class="text-[12px] text-[#9ca3af]">{{ ms.date }}</div>
                    </div>
                  </div>
                </div>
              </div>
            </template>

            <div class="flex justify-end mt-[24px] pt-[16px] border-t border-[#e5e7eb]">
              <button
                class="px-[24px] py-[10px] rounded-[24px] text-[14px] font-medium bg-[#2563eb] text-white hover:bg-[#1d4ed8] cursor-pointer transition-colors"
                @click="closeDialog"
              >关闭</button>
            </div>
          </div>
        </div>
      </Teleport>

    </div>
  </LayoutTeacher>
</template>
