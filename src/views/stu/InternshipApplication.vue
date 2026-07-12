<!--
页面编号：STU-APP-01
页面名称：实习申请表
路由：/stu/internships/new
Figma Node：8:59
当前阶段：阶段一-原型还原
-->
<script setup>
import { ref, reactive } from 'vue'
import LayoutBackend from '../../layouts/LayoutBackend.vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 表单数据
const form = reactive({
  name: '张同学',
  studentId: '2024001001',
  className: '软件工程2401班',
  major: '软件工程',
  companyName: '',
  companyAddress: '',
  contactPerson: '',
  contactPhone: '',
  startDate: '',
  endDate: '',
  position: '',
  reason: '',
})

// 材料上传状态
const materials = reactive([
  { name: '个人简历', status: '已上传' },
  { name: '单位接受函', status: '待上传' },
  { name: '家长知情回执', status: '待上传' },
  { name: '实习保险/安全承诺', status: '待上传' },
])

// 统计卡片数据
const summaryCards = [
  { label: '当前计划', value: '1个', desc: '2024届毕业实习', color: '#2563EB' },
  { label: '申请状态', value: '审核中', desc: '等待学院审核', color: '#F59E0B' },
  { label: '已交材料', value: '1/4项', desc: '仍需补充3项', color: '#16A34A' },
  { label: '距离开始', value: '12天', desc: '06月01日开始', color: '#6B7280' },
]

function submitForm() {
  // TODO: 阶段二接入真实 API
  alert('申请已提交（mock）')
  router.push('/stu/internships')
}

function saveDraft() {
  // TODO: 阶段二接入草稿存储
  alert('草稿已保存（mock）')
  router.push('/stu/internships')
}

function cancel() {
  router.push('/stu/internships')
}

function uploadMaterial(index) {
  // TODO: 阶段二接入真实上传
  if (materials[index].status === '已上传') return
  materials[index].status = '已上传'
  // 更新统计卡片
  const uploaded = materials.filter(m => m.status === '已上传').length
  summaryCards[2].value = `${uploaded}/4项`
  summaryCards[2].desc = uploaded === 4 ? '全部已提交' : `仍需补充${4 - uploaded}项`
}
</script>

<template>
  <LayoutBackend>
    <div class="p-[30px_40px]">

      <!-- ===== 页面标题 ===== -->
      <h1 class="text-[32px] font-bold text-black mb-[4px]">我的毕业实习申请</h1>
      <p class="text-[16px] text-[#757575] mb-[24px]">查看当前可申请计划、申请状态、审核进度与补充材料提醒</p>

      <!-- ===== 统计卡片组（4 张） ===== -->
      <div class="grid grid-cols-4 gap-[22px] mb-[24px]">
        <div
          v-for="card in summaryCards"
          :key="card.label"
          class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] p-[20px_24px] flex flex-col items-center justify-center min-h-[120px]"
        >
          <span class="text-[13px] text-[#757575] mb-[6px]">{{ card.label }}</span>
          <span class="text-[28px] font-bold" :style="{ color: card.color }">{{ card.value }}</span>
          <span class="text-[13px] text-[#9ca3af] mt-[4px]">{{ card.desc }}</span>
        </div>
      </div>

      <!-- ===== 表单主体：双栏 ===== -->
      <div class="grid grid-cols-2 gap-[22px] mb-[22px]">

        <!-- 左栏：基本信息 -->
        <div class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] p-[24px]">
          <h2 class="text-[18px] font-semibold mb-[20px]">基本信息</h2>

          <div class="mb-[16px]">
            <label class="block text-[14px] text-[#374151] mb-[6px]">姓名</label>
            <input v-model="form.name" type="text" placeholder="请输入姓名"
              class="w-full h-[40px] bg-white border border-[rgba(0,0,0,0.16)] rounded-[8px] px-[12px] text-[14px] outline-none focus:border-[#2563eb] placeholder-[#9ca3af]" />
          </div>

          <div class="mb-[16px]">
            <label class="block text-[14px] text-[#374151] mb-[6px]">学号</label>
            <input v-model="form.studentId" type="text" placeholder="请输入学号"
              class="w-full h-[40px] bg-white border border-[rgba(0,0,0,0.16)] rounded-[8px] px-[12px] text-[14px] outline-none focus:border-[#2563eb] placeholder-[#9ca3af]" />
          </div>

          <div class="mb-[16px]">
            <label class="block text-[14px] text-[#374151] mb-[6px]">班级</label>
            <input v-model="form.className" type="text" placeholder="请输入班级"
              class="w-full h-[40px] bg-white border border-[rgba(0,0,0,0.16)] rounded-[8px] px-[12px] text-[14px] outline-none focus:border-[#2563eb] placeholder-[#9ca3af]" />
          </div>

          <div>
            <label class="block text-[14px] text-[#374151] mb-[6px]">专业</label>
            <input v-model="form.major" type="text" placeholder="请输入专业"
              class="w-full h-[40px] bg-white border border-[rgba(0,0,0,0.16)] rounded-[8px] px-[12px] text-[14px] outline-none focus:border-[#2563eb] placeholder-[#9ca3af]" />
          </div>
        </div>

        <!-- 右栏：实习单位信息 -->
        <div class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] p-[24px]">
          <h2 class="text-[18px] font-semibold mb-[20px]">实习单位信息</h2>

          <div class="mb-[16px]">
            <label class="block text-[14px] text-[#374151] mb-[6px]">单位名称</label>
            <input v-model="form.companyName" type="text" placeholder="请输入单位名称"
              class="w-full h-[40px] bg-white border border-[rgba(0,0,0,0.16)] rounded-[8px] px-[12px] text-[14px] outline-none focus:border-[#2563eb] placeholder-[#9ca3af]" />
          </div>

          <div class="mb-[16px]">
            <label class="block text-[14px] text-[#374151] mb-[6px]">单位地址</label>
            <input v-model="form.companyAddress" type="text" placeholder="请输入单位地址"
              class="w-full h-[40px] bg-white border border-[rgba(0,0,0,0.16)] rounded-[8px] px-[12px] text-[14px] outline-none focus:border-[#2563eb] placeholder-[#9ca3af]" />
          </div>

          <div class="mb-[16px]">
            <label class="block text-[14px] text-[#374151] mb-[6px]">联系人</label>
            <input v-model="form.contactPerson" type="text" placeholder="请输入联系人"
              class="w-full h-[40px] bg-white border border-[rgba(0,0,0,0.16)] rounded-[8px] px-[12px] text-[14px] outline-none focus:border-[#2563eb] placeholder-[#9ca3af]" />
          </div>

          <div>
            <label class="block text-[14px] text-[#374151] mb-[6px]">联系电话</label>
            <input v-model="form.contactPhone" type="text" placeholder="请输入联系电话"
              class="w-full h-[40px] bg-white border border-[rgba(0,0,0,0.16)] rounded-[8px] px-[12px] text-[14px] outline-none focus:border-[#2563eb] placeholder-[#9ca3af]" />
          </div>
        </div>
      </div>

      <!-- ===== 表单主体：双栏 第二排 ===== -->
      <div class="grid grid-cols-2 gap-[22px] mb-[22px]">

        <!-- 左栏：实习安排 -->
        <div class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] p-[24px]">
          <h2 class="text-[18px] font-semibold mb-[20px]">实习安排</h2>

          <div class="mb-[16px]">
            <label class="block text-[14px] text-[#374151] mb-[6px]">开始日期</label>
            <input v-model="form.startDate" type="date"
              class="w-full h-[40px] bg-white border border-[rgba(0,0,0,0.16)] rounded-[8px] px-[12px] text-[14px] outline-none focus:border-[#2563eb] text-[#374151]" />
          </div>

          <div class="mb-[16px]">
            <label class="block text-[14px] text-[#374151] mb-[6px]">结束日期</label>
            <input v-model="form.endDate" type="date"
              class="w-full h-[40px] bg-white border border-[rgba(0,0,0,0.16)] rounded-[8px] px-[12px] text-[14px] outline-none focus:border-[#2563eb] text-[#374151]" />
          </div>

          <div>
            <label class="block text-[14px] text-[#374151] mb-[6px]">实习岗位</label>
            <input v-model="form.position" type="text" placeholder="请输入岗位名称"
              class="w-full h-[40px] bg-white border border-[rgba(0,0,0,0.16)] rounded-[8px] px-[12px] text-[14px] outline-none focus:border-[#2563eb] placeholder-[#9ca3af]" />
          </div>
        </div>

        <!-- 右栏：申请理由 -->
        <div class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] p-[24px]">
          <h2 class="text-[18px] font-semibold mb-[20px]">申请理由</h2>
          <textarea v-model="form.reason" placeholder="请简要说明申请实习的原因和期望..."
            class="w-full h-[155px] bg-white border border-[rgba(0,0,0,0.16)] rounded-[8px] p-[12px] text-[14px] outline-none focus:border-[#2563eb] placeholder-[#9ca3af] resize-none"
          ></textarea>
        </div>
      </div>

      <!-- ===== 材料上传 ===== -->
      <div class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] p-[24px] mb-[24px]">
        <h2 class="text-[18px] font-semibold mb-[20px]">材料上传</h2>
        <div class="grid grid-cols-4 gap-[60px]">
          <div v-for="(m, i) in materials" :key="m.name" class="flex flex-col items-center gap-[8px]">
            <span class="text-[14px] text-[#374151]">{{ m.name }}</span>
            <span
              class="inline-block px-[10px] py-[2px] rounded-[12px] text-[12px] font-medium"
              :class="m.status === '已上传'
                ? 'text-[#16a34a] bg-[rgba(20,141,61,0.25)]'
                : 'text-[#f59e0b] bg-[rgba(245,158,11,0.1)]'"
            >{{ m.status }}</span>
            <button
              class="text-[13px] font-medium px-[12px] py-[4px] rounded-[6px] transition-colors"
              :class="m.status === '已上传'
                ? 'text-[#9ca3af] bg-[#f3f4f6] cursor-not-allowed'
                : 'text-[#2563eb] bg-[rgba(37,99,235,0.08)] hover:bg-[rgba(37,99,235,0.15)] cursor-pointer'"
              @click="uploadMaterial(i)"
            >{{ m.status === '已上传' ? '已上传' : '去上传' }}</button>
          </div>
        </div>
      </div>

      <!-- ===== 操作按钮 ===== -->
      <div class="flex items-center gap-[16px]">
        <button
          class="bg-[#2563eb] text-white text-[15px] font-medium px-[36px] py-[12px] rounded-[24px] hover:bg-[#1d4ed8] transition-colors cursor-pointer"
          @click="submitForm"
        >提交申请</button>
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
