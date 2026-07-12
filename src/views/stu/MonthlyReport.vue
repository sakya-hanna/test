<!--
页面编号：STU-RPT-02
页面名称：实习月报提交
路由：/stu/reports/monthly/new
Figma Node：67:15
当前阶段：阶段一-原型还原
-->
<script setup>
import { ref, reactive } from 'vue'
import LayoutBackend from '../../layouts/LayoutBackend.vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const monthOptions = [
  { label: '6月', value: 6 },
  { label: '7月', value: 7 },
]
const selectedMonth = ref(monthOptions[0])

const form = reactive({
  workSummary: '',
  skillGains: '',
})

const attachments = ref([{ name: '6月实习月报.docx', size: '512KB' }])
const totalChars = ref(0)

function updateCharCount() {
  totalChars.value = form.workSummary.length + form.skillGains.length
}

function removeAttachment(index) { attachments.value.splice(index, 1) }
function addAttachment() { attachments.value.push({ name: `月报附件_${attachments.value.length + 1}.pdf`, size: '256KB' }) }

const historyReports = [
  { month: '5月月报', summary: '完成了XX功能模块，参与了N次技术评审...', status: '已提交', date: '2026-05-31' },
  { month: '4月月报', summary: '完成了XX功能模块，参与了N次技术评审...', status: '已提交', date: '2026-04-30' },
]

function submitReport() { alert('月报已提交（mock）'); router.push('/stu/internships/1') }
function saveDraft() { alert('草稿已保存（mock）') }
function cancel() { router.back() }
</script>

<template>
  <LayoutBackend>
    <div class="p-[30px_40px]">

      <h1 class="text-[32px] font-bold text-black mb-[4px]">实习月报提交</h1>
      <p class="text-[16px] text-[#757575] mb-[20px]">提交每月实习月报，总结工作成果与技能成长</p>

      <!-- 信息栏 -->
      <div class="flex items-center gap-[40px] bg-white rounded-[12px] border border-[rgba(0,0,0,0.16)] px-[24px] py-[14px] text-[14px] mb-[20px]">
        <div><span class="text-[#757575]">实习单位：</span><span class="font-medium">华为技术有限公司</span></div>
        <div><span class="text-[#757575]">实习岗位：</span><span class="font-medium">软件开发工程师</span></div>
        <div><span class="text-[#757575]">当前月份：</span><span class="font-medium">6月</span></div>
        <div><span class="text-[#757575]">本月状态：</span><span class="text-[#16a34a] font-medium">进行中</span></div>
      </div>

      <div class="grid grid-cols-[1fr_370px] gap-[22px] mb-[22px]">

        <!-- 左：月报表单 -->
        <div class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] p-[24px]">
          <label class="block text-[14px] text-[#374151] mb-[6px]">选择月份</label>
          <select v-model="selectedMonth"
            class="w-[210px] h-[36px] bg-white border border-[rgba(0,0,0,0.16)] rounded-[8px] px-[12px] text-[14px] outline-none focus:border-[#2563eb] mb-[20px]">
            <option v-for="m in monthOptions" :key="m.value" :value="m">{{ m.label }}</option>
          </select>

          <!-- 工作综述 -->
          <label class="block text-[14px] text-[#374151] mb-[6px]">本月工作综述</label>
          <textarea v-model="form.workSummary" @input="updateCharCount"
            placeholder="概述本月主要工作任务、参与的项目、承担的角色..."
            class="w-full h-[100px] bg-white border border-[rgba(0,0,0,0.16)] rounded-[8px] p-[14px] text-[14px] outline-none focus:border-[#2563eb] placeholder-[#9ca3af] resize-none mb-[20px]"
          ></textarea>

          <!-- 技能提升 -->
          <label class="block text-[14px] text-[#374151] mb-[6px]">技能提升与收获</label>
          <textarea v-model="form.skillGains" @input="updateCharCount"
            placeholder="本月学到的新技能、工具、方法论，个人成长与反思..."
            class="w-full h-[100px] bg-white border border-[rgba(0,0,0,0.16)] rounded-[8px] p-[14px] text-[14px] outline-none focus:border-[#2563eb] placeholder-[#9ca3af] resize-none mb-[16px]"
          ></textarea>

          <!-- 附件 -->
          <label class="block text-[14px] text-[#374151] mb-[8px]">附件上传</label>
          <div class="flex items-center gap-[12px] mb-[12px]">
            <button class="text-[#2563eb] text-[13px] font-medium px-[16px] py-[6px] rounded-[6px] border border-[#2563eb] hover:bg-[#eff6ff] transition-colors cursor-pointer" @click="addAttachment">+ 上传文件</button>
            <span class="text-[12px] text-[#9ca3af]">支持 Word/PDF/图片，最大 10MB</span>
          </div>
          <div class="flex flex-wrap gap-[8px]">
            <div v-for="(file, i) in attachments" :key="i"
              class="inline-flex items-center gap-[6px] bg-[#f3f4f6] rounded-[6px] px-[10px] py-[4px] text-[12px]">
              <span>📎 {{ file.name }}</span>
              <button class="text-[#9ca3af] hover:text-[#ff383c] cursor-pointer ml-[4px]" @click="removeAttachment(i)">✕</button>
            </div>
          </div>
        </div>

        <!-- 右：历史月报 -->
        <div class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] p-[24px]">
          <h3 class="text-[16px] font-semibold mb-[16px]">历史月报</h3>
          <div v-for="(r, i) in historyReports" :key="i" class="mb-[12px] p-[14px] bg-[#f9fafb] rounded-[10px] last:mb-0">
            <div class="flex items-center justify-between mb-[4px]">
              <span class="text-[14px] font-medium">{{ r.month }}</span>
              <div class="flex items-center gap-[8px]">
                <span class="text-[11px] font-medium px-[6px] py-[1px] rounded-[10px] text-[#16a34a] bg-[rgba(20,141,61,0.25)]">{{ r.status }}</span>
                <span class="text-[11px] text-[#9ca3af]">{{ r.date }}</span>
              </div>
            </div>
            <p class="text-[12px] text-[#9ca3af] truncate">{{ r.summary }}</p>
          </div>

          <div class="mt-[24px] bg-[#eff6ff] rounded-[10px] p-[14px] text-[12px] text-[#2563eb]">
            <div>💡 月报需包含整月工作综述和技能收获两部分</div>
            <div class="mt-[4px]">建议参考往期月报格式，每月最后一天前提交</div>
            <div class="mt-[4px]">提交后将由学校指导老师和企业导师分别审阅</div>
          </div>

          <div class="mt-[16px] text-right text-[12px] text-[#9ca3af]">字数统计：{{ totalChars }} 字</div>
        </div>
      </div>

      <!-- 按钮 -->
      <div class="flex items-center gap-[16px]">
        <button class="bg-[#2563eb] text-white text-[15px] font-medium px-[36px] py-[12px] rounded-[24px] hover:bg-[#1d4ed8] transition-colors cursor-pointer" @click="submitReport">提交月报</button>
        <button class="bg-white text-[#2563eb] text-[15px] font-medium px-[36px] py-[12px] rounded-[24px] border border-[#2563eb] hover:bg-[#eff6ff] transition-colors cursor-pointer" @click="saveDraft">保存草稿</button>
        <button class="bg-white text-[#6b7280] text-[15px] font-medium px-[36px] py-[12px] rounded-[24px] border border-[rgba(0,0,0,0.25)] hover:bg-[#f9fafb] transition-colors cursor-pointer" @click="cancel">取消</button>
      </div>

    </div>
  </LayoutBackend>
</template>
