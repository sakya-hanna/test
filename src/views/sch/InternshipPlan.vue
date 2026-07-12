<!--
页面编号：SCH-INTRN-01
页面名称：实习计划发布
路由：/school/internship-plans
Figma Node：111:15
当前阶段：阶段一-原型还原
-->
<script setup>
import { ref, computed } from 'vue'
import LayoutSchool from '../../layouts/LayoutSchool.vue'
import { internshipPlanList, planStatusMap } from '../../mock/sch.js'

const activeTab = ref('')
const showForm = ref(false)

// 发布表单
const form = ref({ title: '', type: '', targetGrade: '', targetDept: '', startDate: '', endDate: '' })

function toggleForm() { showForm.value = !showForm.value; if (!showForm.value) resetForm() }
function resetForm() {
  form.value = { title: '', type: '', targetGrade: '', targetDept: '', startDate: '', endDate: '' }
}
function publish() {
  if (!form.value.title || !form.value.type) return
  internshipPlanList.unshift({
    id: Date.now(), ...form.value, publishDate: new Date().toISOString().slice(0,10), status: '已发布', studentCount: 0
  })
  toggleForm()
}
function editPlan(item) { alert(`编辑实习计划（演示）：${item.title}`) }
function deletePlan(item) { alert(`删除实习计划（演示）：${item.title}`) }

const filteredList = computed(() => {
  let list = internshipPlanList
  if (activeTab.value) list = list.filter(i => i.status === activeTab.value)
  return list
})

const typeOptions = ['毕业实习', '灵活实习', '认知实习', '社会实践']
const statusTabs = [
  { label: '全部', value: '' },
  { label: '已发布', value: '已发布' },
  { label: '草稿', value: '草稿' },
]
</script>

<template>
  <LayoutSchool>
    <div class="p-[30px_40px]">

      <div class="flex items-start justify-between mb-[24px]">
        <div>
          <h1 class="text-[32px] font-bold text-black mb-[4px]">实习计划发布</h1>
          <p class="text-[16px] text-[#757575]">制定与发布全校实习计划安排</p>
        </div>
        <button
          class="bg-[#2563eb] text-white text-[15px] font-medium px-[24px] py-[10px] rounded-[24px] hover:bg-[#1d4ed8] transition-colors shrink-0 cursor-pointer"
          @click="toggleForm"
        >{{ showForm ? '取消' : '+ 新建计划' }}</button>
      </div>

      <!-- 内联发布表单 -->
      <div v-if="showForm" class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] p-[24px] mb-[22px]">
        <h2 class="text-[16px] font-semibold mb-[16px]">新建实习计划</h2>
        <div class="grid grid-cols-3 gap-[16px] mb-[16px]">
          <div>
            <label class="text-[13px] text-[#6b7280] mb-[4px] block">计划标题 <span class="text-[#ff383c]">*</span></label>
            <input v-model="form.title" placeholder="请输入计划标题" class="w-full h-[36px] bg-white border border-[rgba(0,0,0,0.16)] rounded-[8px] px-[12px] text-[14px] outline-none focus:border-[#2563eb] placeholder-[#9ca3af]" />
          </div>
          <div>
            <label class="text-[13px] text-[#6b7280] mb-[4px] block">实习类型 <span class="text-[#ff383c]">*</span></label>
            <select v-model="form.type" class="w-full h-[36px] bg-white border border-[rgba(0,0,0,0.16)] rounded-[8px] px-[12px] text-[14px] outline-none focus:border-[#2563eb]">
              <option value="">请选择</option>
              <option v-for="t in typeOptions" :key="t" :value="t">{{ t }}</option>
            </select>
          </div>
          <div>
            <label class="text-[13px] text-[#6b7280] mb-[4px] block">目标年级</label>
            <input v-model="form.targetGrade" placeholder="如：2023级" class="w-full h-[36px] bg-white border border-[rgba(0,0,0,0.16)] rounded-[8px] px-[12px] text-[14px] outline-none focus:border-[#2563eb] placeholder-[#9ca3af]" />
          </div>
          <div>
            <label class="text-[13px] text-[#6b7280] mb-[4px] block">目标院系</label>
            <input v-model="form.targetDept" placeholder="如：全校" class="w-full h-[36px] bg-white border border-[rgba(0,0,0,0.16)] rounded-[8px] px-[12px] text-[14px] outline-none focus:border-[#2563eb] placeholder-[#9ca3af]" />
          </div>
          <div>
            <label class="text-[13px] text-[#6b7280] mb-[4px] block">开始时间</label>
            <input v-model="form.startDate" type="date" class="w-full h-[36px] bg-white border border-[rgba(0,0,0,0.16)] rounded-[8px] px-[12px] text-[14px] outline-none focus:border-[#2563eb]" />
          </div>
          <div>
            <label class="text-[13px] text-[#6b7280] mb-[4px] block">结束时间</label>
            <input v-model="form.endDate" type="date" class="w-full h-[36px] bg-white border border-[rgba(0,0,0,0.16)] rounded-[8px] px-[12px] text-[14px] outline-none focus:border-[#2563eb]" />
          </div>
        </div>
        <div class="flex justify-end gap-[12px]">
          <button class="px-[20px] py-[8px] rounded-[24px] text-[13px] font-medium bg-white border border-[rgba(0,0,0,0.25)] text-[#6b7280] hover:bg-[#f9fafb] cursor-pointer" @click="resetForm">重置</button>
          <button class="px-[20px] py-[8px] rounded-[24px] text-[13px] font-medium bg-white border border-[#2563eb] text-[#2563eb] hover:bg-[#eff6ff] cursor-pointer" @click="publish">保存草稿</button>
          <button class="px-[20px] py-[8px] rounded-[24px] text-[13px] font-medium bg-[#2563eb] text-white hover:bg-[#1d4ed8] cursor-pointer disabled:opacity-40" :disabled="!form.title || !form.type" @click="publish">发布计划</button>
        </div>
      </div>

      <!-- 筛选 -->
      <div class="flex items-center gap-[42px] mb-[16px]">
        <button v-for="tab in statusTabs" :key="tab.value"
          class="text-[14px] pb-[8px] border-b-[3px] transition-colors cursor-pointer"
          :class="activeTab === tab.value ? 'text-[#2563eb] border-[#2563eb] font-medium' : 'text-[#757575] border-transparent hover:text-[#374151]'"
          @click="activeTab = tab.value">{{ tab.label }}</button>
      </div>
      <div class="border-t border-[#e5e7eb] mb-0" />

      <!-- 列表 -->
      <div class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] overflow-hidden">
        <div class="grid grid-cols-[1fr_100px_120px_120px_140px_100px_90px_90px] gap-[10px] px-[20px] py-[14px] bg-[#f9fafb] text-[13px] font-medium text-[#6b7280] border-b border-[#e5e7eb]">
          <div>计划标题</div><div>类型</div><div>目标年级</div><div>目标院系</div><div>起止时间</div><div>状态</div><div>人数</div><div>操作</div>
        </div>
        <div v-if="filteredList.length === 0" class="text-center py-[60px] text-[#9ca3af] text-[14px]">暂无实习计划</div>
        <div v-for="(item, idx) in filteredList" :key="item.id"
          class="grid grid-cols-[1fr_100px_120px_120px_140px_100px_90px_90px] gap-[10px] px-[20px] py-[16px] text-[14px] items-center hover:bg-[#f9fafb] transition-colors"
          :class="{ 'border-t border-[#f3f4f6]': idx > 0 }">
          <div class="font-medium truncate">{{ item.title }}</div>
          <div><span class="text-[12px] px-[6px] py-[1px] rounded-[6px] bg-[rgba(37,99,235,0.08)] text-[#2563eb]">{{ item.type }}</span></div>
          <div class="text-[#757575]">{{ item.targetGrade }}</div>
          <div class="text-[#757575]">{{ item.targetDept }}</div>
          <div class="text-[12px] text-[#757575]">{{ item.startDate }} ~ {{ item.endDate }}</div>
          <div>
            <span class="inline-block px-[10px] py-[2px] rounded-[12px] text-[12px] font-medium"
              :style="{ color: planStatusMap[item.status]?.color, background: planStatusMap[item.status]?.bg }">{{ item.status }}</span>
          </div>
          <div><span class="font-medium text-[#2563eb]">{{ item.studentCount }}</span> 人</div>
          <div class="flex gap-[8px]">
            <button @click="editPlan(item)" class="text-[#2563eb] text-[12px] font-medium hover:underline cursor-pointer">编辑</button>
            <button @click="deletePlan(item)" class="text-[#ff383c] text-[12px] font-medium hover:underline cursor-pointer">删除</button>
          </div>
        </div>
      </div>

    </div>
  </LayoutSchool>
</template>
