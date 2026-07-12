<!--
页面编号：STU-PAR-01
页面名称：家长知情
路由：/stu/parent-notice
当前阶段：阶段二-静态可验收
-->
<script setup>
import { ref } from 'vue'
import LayoutBackend from '../../layouts/LayoutBackend.vue'
import { parentNoticeData } from '../../mock/stu.js'

const { status, student, internship, parent, notice, receipt } = parentNoticeData
const previewVisible = ref(false)
const fileInput = ref(null)
const currentReceipt = ref({ ...receipt })
const uploadMessage = ref('')

function togglePreview() {
  previewVisible.value = !previewVisible.value
}

function downloadNotice() {
  const content = `${notice.title}\n\n${notice.summary}\n\n学生：${student.name}\n实习单位：${internship.company}`
  const blob = new Blob([content], { type: 'text/plain;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `${notice.title}.txt`
  link.click()
  URL.revokeObjectURL(url)
}

function openFilePicker() {
  fileInput.value?.click()
}

function handleFileChange(event) {
  const file = event.target.files?.[0]
  if (!file) return
  currentReceipt.value = {
    ...currentReceipt.value,
    fileName: file.name,
    uploadedAt: '刚刚',
    fileSize: `${(file.size / 1024 / 1024).toFixed(1)} MB`,
  }
  uploadMessage.value = '新回执已选择，提交后将重新进入审核。'
}
</script>

<template>
  <LayoutBackend>
    <div class="p-[30px_40px] pb-[42px] max-w-[1288px]">
      <div class="flex items-start justify-between mb-[24px]">
        <div>
          <h1 class="text-[32px] leading-[42px] font-bold text-[#111827] mb-[4px]">家长知情</h1>
          <p class="text-[16px] text-[#757575]">查看实习告知书并提交家长签字回执</p>
        </div>
        <div class="flex items-center gap-[8px] rounded-full bg-[#fff7ed] px-[14px] py-[8px] text-[13px] text-[#c2410c] border border-[#fed7aa]">
          <span class="w-[8px] h-[8px] rounded-full bg-[#f97316]" />
          当前状态：{{ status.label }}
        </div>
      </div>

      <div class="grid grid-cols-3 gap-[18px] mb-[22px]">
        <div class="bg-white rounded-[15px] border border-[#e5e7eb] p-[20px_22px] shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
          <div class="text-[13px] text-[#6b7280] mb-[9px]">当前实习</div>
          <div class="text-[18px] font-semibold text-[#111827] truncate">{{ internship.company }}</div>
          <div class="text-[13px] text-[#6b7280] mt-[5px]">{{ internship.position }}</div>
        </div>
        <div class="bg-white rounded-[15px] border border-[#e5e7eb] p-[20px_22px] shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
          <div class="text-[13px] text-[#6b7280] mb-[9px]">确认状态</div>
          <div class="flex items-center gap-[9px]">
            <span class="text-[18px] font-semibold text-[#ea580c]">{{ status.label }}</span>
            <span class="rounded-full bg-[#fff7ed] px-[9px] py-[3px] text-[12px] text-[#c2410c]">已提交</span>
          </div>
          <div class="text-[13px] text-[#6b7280] mt-[5px]">{{ status.description }}</div>
        </div>
        <div class="bg-white rounded-[15px] border border-[#e5e7eb] p-[20px_22px] shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
          <div class="text-[13px] text-[#6b7280] mb-[9px]">回执截止日期</div>
          <div class="text-[18px] font-semibold text-[#111827]">{{ status.deadline }}</div>
          <div class="text-[13px] text-[#16a34a] mt-[5px]">已在截止日期前提交</div>
        </div>
      </div>

      <div class="grid grid-cols-[1.08fr_0.92fr] gap-[22px] mb-[22px] items-start">
        <section class="bg-white rounded-[15px] border border-[#e5e7eb] shadow-[0_2px_8px_rgba(15,23,42,0.04)] overflow-hidden">
          <div class="px-[24px] py-[18px] border-b border-[#eef0f3] flex items-center justify-between">
            <h2 class="text-[18px] font-semibold text-[#111827]">实习与联系人信息</h2>
            <span class="text-[12px] text-[#9ca3af]">信息来源：实习申请</span>
          </div>
          <div class="p-[24px] grid grid-cols-2 gap-x-[30px] gap-y-[22px] text-[14px]">
            <div><div class="text-[#9ca3af] mb-[6px]">学生姓名</div><div class="font-medium text-[#1f2937]">{{ student.name }}</div></div>
            <div><div class="text-[#9ca3af] mb-[6px]">学号</div><div class="font-medium text-[#1f2937]">{{ student.studentNo }}</div></div>
            <div><div class="text-[#9ca3af] mb-[6px]">专业班级</div><div class="font-medium text-[#1f2937]">{{ student.majorClass }}</div></div>
            <div><div class="text-[#9ca3af] mb-[6px]">实习周期</div><div class="font-medium text-[#1f2937]">{{ internship.period }}</div></div>
            <div class="col-span-2"><div class="text-[#9ca3af] mb-[6px]">实习地点</div><div class="font-medium text-[#1f2937]">{{ internship.address }}</div></div>
          </div>
          <div class="mx-[24px] border-t border-[#eef0f3]" />
          <div class="p-[24px]">
            <h3 class="text-[15px] font-semibold text-[#111827] mb-[16px]">家长信息</h3>
            <div class="grid grid-cols-3 gap-[20px] text-[14px]">
              <div><div class="text-[#9ca3af] mb-[6px]">家长姓名</div><div class="font-medium">{{ parent.name }}</div></div>
              <div><div class="text-[#9ca3af] mb-[6px]">与学生关系</div><div class="font-medium">{{ parent.relation }}</div></div>
              <div><div class="text-[#9ca3af] mb-[6px]">联系电话</div><div class="font-medium">{{ parent.phone }}</div></div>
            </div>
          </div>
        </section>

        <section class="bg-white rounded-[15px] border border-[#e5e7eb] shadow-[0_2px_8px_rgba(15,23,42,0.04)] overflow-hidden">
          <div class="px-[24px] py-[18px] border-b border-[#eef0f3]">
            <h2 class="text-[18px] font-semibold text-[#111827]">家长告知书</h2>
          </div>
          <div class="p-[24px]">
            <div class="rounded-[12px] bg-[#f8fafc] border border-[#e5e7eb] p-[20px] mb-[18px]">
              <div class="flex items-start gap-[14px]">
                <div class="w-[42px] h-[48px] rounded-[8px] bg-[#e8f0ff] text-[#2563eb] flex items-center justify-center text-[12px] font-bold shrink-0">PDF</div>
                <div class="min-w-0">
                  <div class="text-[15px] font-semibold text-[#111827] mb-[4px]">{{ notice.title }}</div>
                  <div class="text-[12px] text-[#9ca3af]">{{ notice.version }} · 更新于 {{ notice.updatedAt }}</div>
                </div>
              </div>
              <p class="text-[13px] leading-[22px] text-[#6b7280] mt-[16px]">{{ notice.summary }}</p>
            </div>
            <div class="flex gap-[12px]">
              <button class="flex-1 h-[40px] rounded-[8px] border border-[#2563eb] text-[#2563eb] text-[14px] font-medium hover:bg-[#eff6ff]" @click="togglePreview">
                {{ previewVisible ? '收起预览' : '预览告知书' }}
              </button>
              <button class="flex-1 h-[40px] rounded-[8px] bg-[#2563eb] text-white text-[14px] font-medium hover:bg-[#1d4ed8]" @click="downloadNotice">下载告知书</button>
            </div>
            <div v-if="previewVisible" class="mt-[16px] rounded-[10px] border border-[#dbeafe] bg-[#eff6ff] p-[16px] text-[13px] leading-[22px] text-[#334155]">
              家长须知：请确认学生实习单位、岗位和时间安排，提醒学生遵守单位规章制度及学校安全要求，并在回执处签字。
            </div>
          </div>
        </section>
      </div>

      <section class="bg-white rounded-[15px] border border-[#e5e7eb] shadow-[0_2px_8px_rgba(15,23,42,0.04)] overflow-hidden">
        <div class="px-[24px] py-[18px] border-b border-[#eef0f3] flex items-center justify-between">
          <div>
            <h2 class="text-[18px] font-semibold text-[#111827]">家长知情回执</h2>
            <p class="text-[12px] text-[#9ca3af] mt-[4px]">支持 PDF、JPG、PNG，单个文件不超过 10MB</p>
          </div>
          <span class="rounded-full bg-[#fff7ed] px-[11px] py-[5px] text-[12px] text-[#c2410c]">{{ status.label }}</span>
        </div>
        <div class="p-[24px] flex items-center gap-[18px]">
          <div class="w-[48px] h-[48px] rounded-[10px] bg-[#f0fdf4] text-[#16a34a] flex items-center justify-center text-[13px] font-bold shrink-0">回执</div>
          <div class="min-w-0 flex-1">
            <div class="text-[15px] font-medium text-[#111827] truncate">{{ currentReceipt.fileName }}</div>
            <div class="text-[12px] text-[#9ca3af] mt-[5px]">{{ currentReceipt.fileSize }} · 上传于 {{ currentReceipt.uploadedAt }} · 审核人：{{ currentReceipt.reviewer }}</div>
            <div v-if="uploadMessage" class="text-[12px] text-[#2563eb] mt-[5px]">{{ uploadMessage }}</div>
          </div>
          <input ref="fileInput" class="hidden" type="file" accept=".pdf,.jpg,.jpeg,.png" @change="handleFileChange">
          <button class="h-[40px] px-[20px] rounded-[8px] border border-[#d1d5db] text-[14px] font-medium text-[#374151] hover:bg-[#f9fafb]" @click="openFilePicker">重新上传</button>
        </div>
      </section>
    </div>
  </LayoutBackend>
</template>
