<!--
页面编号：STU-RPT-01
页面名称：实习周报提交
路由：/stu/reports/weekly/new
Figma Node：54:59
当前阶段：阶段一-原型还原
-->
<script setup>
import { ref, reactive } from 'vue'
import LayoutBackend from '../../layouts/LayoutBackend.vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 周次选项（mock）
const weekOptions = [
  { label: '第3周（06/02 - 06/08）', value: 3 },
  { label: '第4周（06/09 - 06/15）', value: 4 },
]
const selectedWeek = ref(weekOptions[0])

// 表单
const form = reactive({
  week: 3,
  content: '',
})

// 附件
const attachments = ref([
  { name: '本周工作总结.docx', size: '256KB' },
  { name: '项目进度表.xlsx', size: '128KB' },
])
const charCount = ref(0)

function updateCharCount() {
  charCount.value = form.content.length
}

// 历史周报
const historyReports = [
  { week: '第2周', summary: '完成了XX模块开发，修复了N个bug...', status: '已提交', date: '2026-05-28' },
  { week: '第1周', summary: '完成了XX模块开发，修复了N个bug...', status: '已提交', date: '2026-05-21' },
]

function removeAttachment(index) {
  attachments.value.splice(index, 1)
}

function addAttachment() {
  // TODO: 阶段二接入真实文件上传
  attachments.value.push({ name: `新附件_${attachments.value.length + 1}.pdf`, size: '512KB' })
}

function submitReport() {
  alert('周报已提交（mock）')
  router.push('/stu/internships/1')
}

function saveDraft() {
  alert('草稿已保存（mock）')
}

function cancel() {
  router.back()
}
</script>

<template>
  <LayoutBackend>
    <div class="p-[30px_40px]">

      <!-- ===== 页面标题 ===== -->
      <h1 class="text-[32px] font-bold text-black mb-[4px]">实习周报提交</h1>
      <p class="text-[16px] text-[#757575] mb-[20px]">提交每周实习周报，记录工作内容与心得</p>

      <!-- ===== 信息栏 ===== -->
      <div class="flex items-center gap-[40px] bg-white rounded-[12px] border border-[rgba(0,0,0,0.16)] px-[24px] py-[14px] text-[14px] mb-[20px]">
        <div><span class="text-[#757575]">实习单位：</span><span class="font-medium">华为技术有限公司</span></div>
        <div><span class="text-[#757575]">实习岗位：</span><span class="font-medium">软件开发工程师</span></div>
        <div><span class="text-[#757575]">当前周次：</span><span class="font-medium">第3周（06/02 - 06/08）</span></div>
        <div><span class="text-[#757575]">本周状态：</span><span class="text-[#16a34a] font-medium">进行中</span></div>
      </div>

      <!-- ===== 双栏：表单 + 历史 ===== -->
      <div class="grid grid-cols-[1fr_370px] gap-[22px] mb-[22px]">

        <!-- 左：周报表单 -->
        <div class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] p-[24px]">
          <!-- 周次选择 -->
          <label class="block text-[14px] text-[#374151] mb-[6px]">选择周次</label>
          <select v-model="selectedWeek"
            class="w-[210px] h-[36px] bg-white border border-[rgba(0,0,0,0.16)] rounded-[8px] px-[12px] text-[14px] outline-none focus:border-[#2563eb] mb-[20px]">
            <option v-for="w in weekOptions" :key="w.value" :value="w">{{ w.label }}</option>
          </select>

          <!-- 工作内容 -->
          <label class="block text-[14px] text-[#374151] mb-[6px]">本周工作内容</label>
          <textarea v-model="form.content" @input="updateCharCount"
            placeholder="请描述本周的主要工作内容、参与的项目、学到的技能、遇到的问题及解决方案..."
            class="w-full h-[200px] bg-white border border-[rgba(0,0,0,0.16)] rounded-[8px] p-[14px] text-[14px] outline-none focus:border-[#2563eb] placeholder-[#9ca3af] resize-none mb-[16px]"
          ></textarea>

          <!-- 附件上传 -->
          <label class="block text-[14px] text-[#374151] mb-[8px]">附件上传</label>
          <div class="flex items-center gap-[12px] mb-[12px]">
            <button
              class="text-[#2563eb] text-[13px] font-medium px-[16px] py-[6px] rounded-[6px] border border-[#2563eb] hover:bg-[#eff6ff] transition-colors cursor-pointer"
              @click="addAttachment"
            >+ 上传文件</button>
            <span class="text-[12px] text-[#9ca3af]">支持 Word/PDF/图片，最大 10MB</span>
          </div>

          <!-- 已上传文件标签 -->
          <div class="flex flex-wrap gap-[8px]">
            <div v-for="(file, i) in attachments" :key="i"
              class="inline-flex items-center gap-[6px] bg-[#f3f4f6] rounded-[6px] px-[10px] py-[4px] text-[12px]"
            >
              <span>📎 {{ file.name }}</span>
              <button class="text-[#9ca3af] hover:text-[#ff383c] cursor-pointer ml-[4px]" @click="removeAttachment(i)">✕</button>
            </div>
          </div>
        </div>

        <!-- 右：历史周报 -->
        <div class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] p-[24px]">
          <h3 class="text-[16px] font-semibold mb-[16px]">历史周报</h3>

          <div v-for="(r, i) in historyReports" :key="i"
            class="mb-[12px] p-[14px] bg-[#f9fafb] rounded-[10px] last:mb-0"
          >
            <div class="flex items-center justify-between mb-[4px]">
              <span class="text-[14px] font-medium">{{ r.week }}</span>
              <div class="flex items-center gap-[8px]">
                <span class="text-[11px] font-medium px-[6px] py-[1px] rounded-[10px] text-[#16a34a] bg-[rgba(20,141,61,0.25)]">{{ r.status }}</span>
                <span class="text-[11px] text-[#9ca3af]">{{ r.date }}</span>
              </div>
            </div>
            <p class="text-[12px] text-[#9ca3af] truncate">{{ r.summary }}</p>
          </div>

          <!-- 提示框 -->
          <div class="mt-[24px] bg-[#eff6ff] rounded-[10px] p-[14px] text-[12px] text-[#2563eb]">
            <div>💡 周报提交后需指导老师审核通过</div>
            <div class="mt-[4px]">审核通过前可随时修改，请于每周日24:00前提交</div>
          </div>

          <!-- 字数统计 -->
          <div class="mt-[16px] text-right text-[12px] text-[#9ca3af]">字数统计：{{ charCount }} 字</div>
        </div>
      </div>

      <!-- ===== 操作按钮 ===== -->
      <div class="flex items-center gap-[16px]">
        <button
          class="bg-[#2563eb] text-white text-[15px] font-medium px-[36px] py-[12px] rounded-[24px] hover:bg-[#1d4ed8] transition-colors cursor-pointer"
          @click="submitReport"
        >提交周报</button>
        <button
          class="bg-white text-[#2563eb] text-[15px] font-medium px-[36px] py-[12px] rounded-[24px] border border-[#2563eb] hover:bg-[#eff6ff] transition-colors cursor-pointer"
          @click="saveDraft"
        >保存草稿</button>
        <button
          class="bg-white text-[#6b7280] text-[15px] font-medium px-[36px] py-[12px] rounded-[24px] border border-[rgba(0,0,0,0.25)] hover:bg-[#f9fafb] transition-colors cursor-pointer"
          @click="cancel"
        >取消</button>
      </div>

    </div>
  </LayoutBackend>
</template>
