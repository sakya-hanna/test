<!--
页面编号：STU-RPT-03
页面名称：实习成果提交
路由：/stu/achievements/new
Figma Node：71:28
当前阶段：阶段一-原型还原
-->
<script setup>
import { reactive } from 'vue'
import LayoutBackend from '../../layouts/LayoutBackend.vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 材料列表
const materials = reactive([
  { name: '实习报告', desc: '毕业实习总结报告，不少于3000字', status: '已上传', file: '毕业实习报告_张同学.docx', required: true },
  { name: '实习鉴定表', desc: '企业指导老师填写并盖章的鉴定表', status: '待上传', file: '', required: true },
  { name: '实习单位回执', desc: '实习单位出具的实习证明回执', status: '审核中', file: '实习回执_扫描件.pdf', required: true },
  { name: '其他补充材料', desc: '获奖证书、项目成果等（选填）', status: '待上传', file: '', required: false },
])

// 进度清单
const progressItems = [
  { name: '个人简历', done: true },
  { name: '单位接受函', done: true },
  { name: '实习周报(4篇)', done: true },
  { name: '实习月报(2篇)', done: true },
  { name: '实习报告', done: false, current: true },
  { name: '实习鉴定表', done: false },
]

function uploadMaterial(index) {
  if (materials[index].status === '已上传' || materials[index].status === '审核中') return
  materials[index].status = '已上传'
  materials[index].file = `${materials[index].name}_文件.pdf`
}
function replaceMaterial(index) {
  // TODO: 阶段二真实替换
  alert(`重新上传 ${materials[index].name}`)
}
function submitAll() { alert('全部材料已提交（mock）'); router.push('/stu/internships/1') }
function saveDraft() { alert('草稿已保存（mock）') }
function cancel() { router.back() }
</script>

<template>
  <LayoutBackend>
    <div class="p-[30px_40px]">

      <h1 class="text-[32px] font-bold text-black mb-[4px]">实习成果提交</h1>
      <p class="text-[16px] text-[#757575] mb-[20px]">提交实习报告、鉴定表等结项材料</p>

      <!-- 截止提醒 -->
      <div class="flex items-center bg-[#fef3c7] border border-[#fcd34d] rounded-[10px] px-[24px] py-[12px] text-[14px] text-[#92400e] mb-[24px]">
        ⏰ 提交截止：2026年6月30日，距截止还有 28 天
      </div>

      <div class="grid grid-cols-[1fr_370px] gap-[22px] mb-[22px]">

        <!-- 左：材料卡片列表 -->
        <div class="space-y-[16px]">
          <div v-for="(m, i) in materials" :key="m.name"
            class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] p-[24px] flex items-center gap-[20px]"
          >
            <!-- 图标 -->
            <div class="w-[44px] h-[44px] rounded-[10px] bg-[#f3f4f6] flex items-center justify-center text-[20px] shrink-0">
              📄
            </div>

            <!-- 信息 -->
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-[10px] mb-[4px]">
                <span class="text-[15px] font-medium">{{ m.name }}</span>
                <span v-if="m.required" class="text-[11px] text-[#ff383c] bg-[rgba(255,56,60,0.1)] px-[6px] py-[1px] rounded-[8px]">必填</span>
                <span v-else class="text-[11px] text-[#9ca3af] bg-[rgba(0,0,0,0.05)] px-[6px] py-[1px] rounded-[8px]">选填</span>
                <span
                  class="text-[11px] font-medium px-[6px] py-[1px] rounded-[8px]"
                  :class="{
                    'text-[#16a34a] bg-[rgba(20,141,61,0.25)]': m.status === '已上传',
                    'text-[#f59e0b] bg-[rgba(245,158,11,0.1)]': m.status === '审核中',
                    'text-[#9ca3af] bg-[rgba(0,0,0,0.05)]': m.status === '待上传',
                  }"
                >{{ m.status }}</span>
              </div>
              <p class="text-[13px] text-[#9ca3af] mb-[8px]">{{ m.desc }}</p>
              <div v-if="m.file" class="inline-flex items-center gap-[6px] bg-[#f3f4f6] rounded-[6px] px-[10px] py-[3px] text-[12px] text-[#374151]">
                📎 {{ m.file }}
              </div>
            </div>

            <!-- 操作 -->
            <button v-if="m.status === '待上传'"
              class="bg-[#2563eb] text-white text-[14px] font-medium px-[20px] py-[8px] rounded-[6px] hover:bg-[#1d4ed8] transition-colors cursor-pointer shrink-0"
              @click="uploadMaterial(i)">上传</button>
            <button v-else
              class="bg-white text-[#2563eb] text-[14px] font-medium px-[20px] py-[8px] rounded-[6px] border border-[#2563eb] hover:bg-[#eff6ff] transition-colors cursor-pointer shrink-0"
              @click="replaceMaterial(i)">更换</button>
          </div>
        </div>

        <!-- 右：提交进度 -->
        <div class="space-y-[16px]">
          <div class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] p-[24px]">
            <h3 class="text-[16px] font-semibold mb-[16px]">提交进度</h3>
            <div class="relative pl-[20px]">
              <div class="absolute left-[4px] top-[4px] bottom-[4px] w-[2px] bg-[#e5e7eb]" />
              <div v-for="(p, i) in progressItems" :key="i" class="relative mb-[20px] last:mb-0">
                <div class="absolute left-[-16px] top-[4px] w-[10px] h-[10px] rounded-full z-10"
                  :class="p.done ? 'bg-[#16a34a]' : p.current ? 'bg-[#2563eb] ring-[3px] ring-[rgba(37,99,235,0.3)]' : 'bg-[#d1d5db]'" />
                <div>
                  <span class="text-[14px]" :class="p.current ? 'text-[#2563eb] font-medium' : p.done ? 'text-[#374151]' : 'text-[#9ca3af]'">{{ p.name }}</span>
                  <span v-if="p.current" class="text-[12px] text-[#2563eb] ml-[8px]">← 当前待提交</span>
                </div>
              </div>
            </div>
          </div>

          <div class="bg-[#eff6ff] rounded-[15px] p-[16px] text-[12px] text-[#2563eb]">
            <div>💡 全部材料审核通过后，实习成绩才会正式发布</div>
            <div class="mt-[4px]">纸质材料需同时提交至学院办公室备案</div>
          </div>
        </div>
      </div>

      <!-- 按钮 -->
      <div class="flex items-center gap-[16px]">
        <button class="bg-[#2563eb] text-white text-[15px] font-medium px-[36px] py-[12px] rounded-[24px] hover:bg-[#1d4ed8] transition-colors cursor-pointer" @click="submitAll">提交全部</button>
        <button class="bg-white text-[#2563eb] text-[15px] font-medium px-[36px] py-[12px] rounded-[24px] border border-[#2563eb] hover:bg-[#eff6ff] transition-colors cursor-pointer" @click="saveDraft">保存草稿</button>
        <button class="bg-white text-[#6b7280] text-[15px] font-medium px-[36px] py-[12px] rounded-[24px] border border-[rgba(0,0,0,0.25)] hover:bg-[#f9fafb] transition-colors cursor-pointer" @click="cancel">取消</button>
      </div>

    </div>
  </LayoutBackend>
</template>
