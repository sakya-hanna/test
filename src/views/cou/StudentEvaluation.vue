<!--
页面编号：COU-EVAL-01
页面名称：学生评价
路由：/counselor/evaluations
说明：辅导员查看所带学生的各方评价结果，包含教师评分、企业评分、综合成绩
  依据需求 3.3.2：学校指导老师评分 + 企业指导老师评分 + 综合成绩计算
-->
<script setup>
import { ref, computed } from 'vue'

const searchQuery = ref('')
const currentFilter = ref('全部')
const selectedStudent = ref(null)
const showDetail = ref(false)

const students = ref([
  {
    id:1, name:'李明', sid:'2024030112', cls:'软件2201', type:'毕业实习', company:'无锡科技有限公司',
    teacherScore:85, enterpriseScore:90, totalScore:87.5, level:'良好', levelColor:'#16A34A', levelBg:'rgba(22,163,74,0.1)',
    status:'已评定', statusBg:'rgba(22,163,74,0.1)', statusColor:'#16A34A',
    teacherDetail:{ name:'王老师(T20215)', scores:{ 态度:88, 技能:85, 学习能力:90, 团队协作:82, 表达沟通:80 }, comment:'该生在实习期间态度端正，能够按时完成周报和月报提交。技术能力扎实，对分配的任务能够独立完成。建议在团队沟通方面进一步加强，多参与团队讨论。' },
    enterpriseDetail:{ name:'陈志远(技术总监)', scores:{ 工作态度:92, 专业技能:88, 团队协作:85, 学习能力:95, 表达能力:86 }, comment:'李明同学工作积极主动，前端开发技能有明显提升，能够独立承担模块开发任务。与团队成员配合良好，建议在技术分享方面更主动一些。' },
  },
  {
    id:2, name:'王芳', sid:'2024030115', cls:'软件2202', type:'企业需求对接', company:'苏州创新技术公司',
    teacherScore:92, enterpriseScore:88, totalScore:90, level:'优秀', levelColor:'#2563EB', levelBg:'rgba(37,99,235,0.1)',
    status:'已评定', statusBg:'rgba(22,163,74,0.1)', statusColor:'#16A34A',
    teacherDetail:{ name:'王老师(T20215)', scores:{ 态度:95, 技能:92, 学习能力:93, 团队协作:90, 表达沟通:90 }, comment:'王芳同学实习表现优秀，企业需求文档整理工作完成出色。逻辑清晰，善于总结归纳，展现了较强的分析和表达能力。' },
    enterpriseDetail:{ name:'李经理(HR经理)', scores:{ 工作态度:90, 专业技能:85, 团队协作:92, 学习能力:88, 表达能力:90 }, comment:'王芳同学工作认真负责，在需求对接过程中展现了良好的沟通能力，能够准确理解企业需求并及时反馈。' },
  },
  {
    id:3, name:'赵强', sid:'2024030120', cls:'计算机2201', type:'毕业实习', company:'南京软件有限公司',
    teacherScore:0, enterpriseScore:0, totalScore:0, level:'—', levelColor:'#9CA3AF', levelBg:'rgba(156,163,175,0.1)',
    status:'待评定', statusBg:'rgba(217,119,6,0.1)', statusColor:'#D97706',
    teacherDetail:null, enterpriseDetail:null,
  },
  {
    id:4, name:'陈雪', sid:'2024030125', cls:'计算机2202', type:'灵活实习', company:'上海数据集团',
    teacherScore:72, enterpriseScore:70, totalScore:71, level:'中等', levelColor:'#D97706', levelBg:'rgba(217,119,6,0.1)',
    status:'已评定', statusBg:'rgba(22,163,74,0.1)', statusColor:'#16A34A',
    teacherDetail:{ name:'李老师(T30156)', scores:{ 态度:75, 技能:70, 学习能力:72, 团队协作:74, 表达沟通:68 }, comment:'陈雪同学基础尚可，但实习主动性有待提高。周报内容较为简单，缺乏深度思考。希望后续能够更加积极地参与实习工作。' },
    enterpriseDetail:{ name:'刘经理', scores:{ 工作态度:68, 专业技能:72, 团队协作:70, 学习能力:70, 表达能力:70 }, comment:'该生需要在指导下才能完成任务，独立解决问题的能力有待加强。建议多向同事请教，主动学习。' },
  },
  {
    id:5, name:'刘洋', sid:'2024030130', cls:'软件2201', type:'认知实习', company:'杭州云科技有限公司',
    teacherScore:90, enterpriseScore:null, totalScore:90, level:'优秀', levelColor:'#2563EB', levelBg:'rgba(37,99,235,0.1)',
    status:'仅教师评定', statusBg:'rgba(37,99,235,0.1)', statusColor:'#2563EB',
    teacherDetail:{ name:'王老师(T20215)', scores:{ 态度:92, 技能:88, 学习能力:93, 团队协作:null, 表达沟通:90 }, comment:'刘洋同学认知实习报告质量很高，对企业技术栈理解到位，对未来职业规划有清晰认识。认知实习不涉及企业评分。' },
    enterpriseDetail:null,
  },
  {
    id:6, name:'周婷', sid:'2024030135', cls:'计算机2201', type:'毕业实习', company:'常州智能装备公司',
    teacherScore:88, enterpriseScore:92, totalScore:90, level:'优秀', levelColor:'#2563EB', levelBg:'rgba(37,99,235,0.1)',
    status:'已评定', statusBg:'rgba(22,163,74,0.1)', statusColor:'#16A34A',
    teacherDetail:{ name:'王老师(T20215)', scores:{ 态度:90, 技能:88, 学习能力:86, 团队协作:90, 表达沟通:85 }, comment:'周婷同学实习期间表现稳定，代码质量较高，在团队中起到了积极作用。' },
    enterpriseDetail:{ name:'赵工(研发主管)', scores:{ 工作态度:95, 专业技能:90, 团队协作:92, 学习能力:88, 表达能力:90 }, comment:'周婷工作认真负责，代码质量高，能够及时发现并反馈问题，是团队中可靠的成员。' },
  },
])

const stats = computed(() => ({
  total: students.value.length,
  rated: students.value.filter(s => s.status === '已评定').length,
  pending: students.value.filter(s => s.status === '待评定').length,
  excellent: students.value.filter(s => s.level === '优秀').length,
}))

const filteredStudents = computed(() => {
  let list = students.value
  if (currentFilter.value === '已评定') list = list.filter(s => s.status === '已评定')
  if (currentFilter.value === '待评定') list = list.filter(s => s.status === '待评定')
  if (searchQuery.value) list = list.filter(s => s.name.includes(searchQuery.value) || s.cls.includes(searchQuery.value))
  return list
})

function openDetail(s) { selectedStudent.value = s; showDetail.value = true }
</script>

<template>
  <div class="flex min-h-screen">
    <!-- 侧边栏 -->
    <aside class="w-[232px] min-h-screen flex-shrink-0 text-white flex flex-col" style="background:#00203D;">
      <div class="px-5 py-5 border-b border-white/10 text-lg font-semibold">实习管理系统</div>
      <nav class="flex-1 py-3 px-3 space-y-0.5">
        <router-link to="/counselor/dashboard" class="flex items-center px-4 py-3 rounded-lg text-[16px] text-white/80 hover:bg-white/10">指导工作台</router-link>
        <router-link to="/counselor/student-list" class="flex items-center px-4 py-3 rounded-lg text-[16px] text-white/80 hover:bg-white/10">我的学生</router-link>
        <router-link to="/counselor/process-feedback" class="flex items-center px-4 py-3 rounded-lg text-[16px] text-white/80 hover:bg-white/10">过程反馈</router-link>
        <router-link to="/counselor/evaluations" class="flex items-center px-4 py-3 rounded-lg text-[16px]" style="color:#C7D2FE;">学生评价</router-link>
        <router-link to="/counselor/notifications" class="flex items-center px-4 py-3 rounded-lg text-[16px] text-white/80 hover:bg-white/10">通知公告</router-link>
      </nav>
    </aside>

    <!-- 右侧主体 -->
    <div class="flex-1 flex flex-col" style="background:#F5F7FB;">
      <header class="h-[72px] bg-white flex items-center justify-between px-6 shadow-sm flex-shrink-0">
        <span class="text-[14px]" style="color:#757575;">首页 / 学生评价</span>
        <div class="flex items-center gap-4">
          <div class="flex items-center gap-2">
            <div class="w-10 h-10 rounded-full flex items-center justify-center text-xs font-medium" style="background:#C8C8C8;">孙</div>
            <div><div class="text-[14px] font-medium text-gray-900">孙辅导员</div><div class="text-[12px]" style="color:#2563EB;">辅导员</div></div>
          </div>
          <router-link to="/login" class="text-[14px] font-medium" style="color:#FF383C;">登出</router-link>
        </div>
      </header>

      <main class="flex-1 overflow-auto px-10 py-6">
        <div class="mb-5">
          <p class="text-[14px]" style="color:#757575;">首页 / 学生评价</p>
          <h2 class="text-[32px] font-bold text-gray-900 mt-1">学生评价</h2>
          <p class="text-[16px] mt-1" style="color:#757575;">查看所带学生的教师评分、企业评分和综合成绩</p>
        </div>

        <!-- 统计卡片 -->
        <div class="grid grid-cols-4 gap-4 mb-5">
          <div class="bg-white rounded-[12px] p-5 cursor-pointer" @click="currentFilter = '全部'">
            <p class="text-[13px] mb-2" style="color:#6B7280;">全部学生</p>
            <p class="text-[28px] font-bold" style="color:#2563EB;">{{ stats.total }}</p>
          </div>
          <div class="bg-white rounded-[12px] p-5 cursor-pointer" @click="currentFilter = '已评定'">
            <p class="text-[13px] mb-2" style="color:#6B7280;">已评定</p>
            <p class="text-[28px] font-bold" style="color:#16A34A;">{{ stats.rated }}</p>
          </div>
          <div class="bg-white rounded-[12px] p-5 cursor-pointer" @click="currentFilter = '待评定'">
            <p class="text-[13px] mb-2" style="color:#6B7280;">待评定</p>
            <p class="text-[28px] font-bold" style="color:#D97706;">{{ stats.pending }}</p>
          </div>
          <div class="bg-white rounded-[12px] p-5">
            <p class="text-[13px] mb-2" style="color:#6B7280;">优秀</p>
            <p class="text-[28px] font-bold" style="color:#2563EB;">{{ stats.excellent }}</p>
          </div>
        </div>

        <!-- 筛选工具栏 -->
        <div class="flex items-center justify-between mb-5">
          <div class="flex items-center gap-3">
            <span class="text-[14px] font-medium text-gray-900">筛选：</span>
            <span v-for="f in ['全部','已评定','待评定']" :key="f"
              class="text-[14px] cursor-pointer px-2 py-1 rounded transition-colors"
              :class="currentFilter === f ? 'font-semibold' : ''"
              :style="{ color: currentFilter === f ? '#2563EB' : '#6B7280' }"
              @click="currentFilter = f">{{ f }}</span>
          </div>
          <input v-model="searchQuery" type="text" placeholder="搜索学生姓名或班级..."
            class="w-[220px] h-[36px] rounded-[8px] border px-3 text-[14px] outline-none"
            style="border-color:rgba(0,0,0,0.16); color:#9CA3AF; background:white;" />
        </div>

        <!-- 数据表格 -->
        <div class="bg-white rounded-[8px] shadow-sm mb-4 overflow-hidden">
          <div class="h-[44px] flex items-center px-4" style="background:#F3F4F6;">
            <div class="text-[12px] font-semibold flex-shrink-0" style="color:#374151; width:80px;">学生姓名</div>
            <div class="text-[12px] font-semibold flex-shrink-0" style="color:#374151; width:100px;">班级</div>
            <div class="text-[12px] font-semibold flex-shrink-0" style="color:#374151; width:100px;">实习类型</div>
            <div class="text-[12px] font-semibold flex-shrink-0" style="color:#374151; width:160px;">实习企业</div>
            <div class="text-[12px] font-semibold flex-shrink-0" style="color:#374151; width:80px;">教师评分</div>
            <div class="text-[12px] font-semibold flex-shrink-0" style="color:#374151; width:80px;">企业评分</div>
            <div class="text-[12px] font-semibold flex-shrink-0" style="color:#374151; width:80px;">综合成绩</div>
            <div class="text-[12px] font-semibold flex-shrink-0" style="color:#374151; width:80px;">等级</div>
            <div class="text-[12px] font-semibold flex-shrink-0" style="color:#374151; width:80px;">状态</div>
            <div class="text-[12px] font-semibold flex-shrink-0" style="color:#374151; width:60px;">操作</div>
          </div>
          <div v-for="(s, i) in filteredStudents" :key="s.id">
            <div class="h-[52px] flex items-center px-4 hover:bg-[#F9FAFB] transition-colors">
              <div class="text-[12px] flex-shrink-0" style="color:#374151; width:80px;">{{ s.name }}</div>
              <div class="text-[12px] flex-shrink-0" style="color:#374151; width:100px;">{{ s.cls }}</div>
              <div class="text-[12px] flex-shrink-0" style="color:#374151; width:100px;">{{ s.type }}</div>
              <div class="text-[12px] flex-shrink-0" style="color:#374151; width:160px;">{{ s.company }}</div>
              <div class="text-[12px] font-semibold flex-shrink-0" style="color:#2563EB; width:80px;">{{ s.teacherScore || '—' }}</div>
              <div class="text-[12px] font-semibold flex-shrink-0" style="color:#16A34A; width:80px;">{{ s.enterpriseScore || '—' }}</div>
              <div class="text-[12px] font-bold flex-shrink-0" style="color:#111827; width:80px;">{{ s.totalScore || '—' }}</div>
              <div class="flex-shrink-0" style="width:80px;">
                <span class="inline-flex items-center rounded-[12px] px-3 py-1 text-[11px] font-medium" :style="{background:s.levelBg, color:s.levelColor}">{{ s.level }}</span>
              </div>
              <div class="flex-shrink-0" style="width:80px;">
                <span class="inline-flex items-center rounded-[12px] px-3 py-1 text-[11px] font-medium" :style="{background:s.statusBg, color:s.statusColor}">{{ s.status }}</span>
              </div>
              <div class="flex-shrink-0" style="width:60px;">
                <span v-if="s.status !== '待评定'" class="text-[13px] font-medium cursor-pointer hover:underline" style="color:#2563EB;" @click="openDetail(s)">详情</span>
                <span v-else class="text-[13px]" style="color:#9CA3AF;">—</span>
              </div>
            </div>
            <div v-if="i < filteredStudents.length - 1" class="h-px mx-4" style="background:#E5E7EB;"></div>
          </div>
          <div v-if="filteredStudents.length === 0" class="h-[120px] flex items-center justify-center text-[14px]" style="color:#9CA3AF;">暂无匹配的记录</div>
        </div>
      </main>
    </div>

    <!-- 评价详情弹窗 -->
    <div v-if="showDetail && selectedStudent" class="fixed inset-0 z-50 flex items-center justify-center" style="background:rgba(0,0,0,0.45);" @click.self="showDetail = false">
      <div class="bg-white rounded-[12px] overflow-hidden" style="width:640px; max-height:85vh; overflow-y:auto;">
        <div class="p-[20px_24px] border-b border-[#E5E7EB] flex items-center justify-between">
          <span class="text-[18px] font-semibold text-[#111827]">评价详情 - {{ selectedStudent.name }}</span>
          <button class="w-[28px] h-[28px] flex items-center justify-center rounded-[6px] text-[18px] text-[#6B7280] hover:bg-[#F3F4F6] border-none bg-none cursor-pointer" @click="showDetail = false">✕</button>
        </div>
        <div class="p-[24px]">
          <!-- 综合成绩 -->
          <div class="flex items-center justify-center gap-8 mb-6 p-4 bg-[#F9FAFB] rounded-[8px]">
            <div class="text-center">
              <div class="text-[11px] mb-1" style="color:#9CA3AF;">学校教师评分</div>
              <div class="text-[32px] font-bold" style="color:#2563EB;">{{ selectedStudent.teacherScore || '—' }}</div>
            </div>
            <div class="text-[24px] font-bold" style="color:#D1D5DB;">+</div>
            <div class="text-center">
              <div class="text-[11px] mb-1" style="color:#9CA3AF;">企业导师评分</div>
              <div class="text-[32px] font-bold" style="color:#16A34A;">{{ selectedStudent.enterpriseScore || '—' }}</div>
            </div>
            <div class="text-[24px] font-bold" style="color:#D1D5DB;">→</div>
            <div class="text-center">
              <div class="text-[11px] mb-1" style="color:#9CA3AF;">综合成绩</div>
              <div class="text-[32px] font-bold" style="color:#111827;">{{ selectedStudent.totalScore || '—' }}</div>
              <div class="text-[13px] font-semibold mt-1" :style="{color:selectedStudent.levelColor}">{{ selectedStudent.level }}</div>
            </div>
          </div>

          <!-- 教师评价 -->
          <div v-if="selectedStudent.teacherDetail" class="mb-4 border border-[#E5E7EB] rounded-[8px] overflow-hidden">
            <div class="px-4 py-2 flex items-center justify-between" style="background:#EFF6FF;">
              <span class="text-[13px] font-semibold" style="color:#2563EB;">学校教师评价 — {{ selectedStudent.teacherDetail.name }}</span>
            </div>
            <div class="p-4">
              <div class="flex flex-wrap gap-3 mb-3">
                <div v-for="(val, key) in selectedStudent.teacherDetail.scores" :key="key" class="bg-[#F9FAFB] rounded-[6px] px-3 py-2 text-center">
                  <div class="text-[10px]" style="color:#9CA3AF;">{{ key }}</div>
                  <div class="text-[16px] font-semibold" style="color:#111827;">{{ val === '—' ? '—' : val }}</div>
                </div>
              </div>
              <p class="text-[13px] leading-relaxed" style="color:#374151;">{{ selectedStudent.teacherDetail.comment }}</p>
            </div>
          </div>

          <!-- 企业评价 -->
          <div v-if="selectedStudent.enterpriseDetail" class="border border-[#E5E7EB] rounded-[8px] overflow-hidden">
            <div class="px-4 py-2 flex items-center justify-between" style="background:#F0FDF4;">
              <span class="text-[13px] font-semibold" style="color:#16A34A;">企业导师评价 — {{ selectedStudent.enterpriseDetail.name }}</span>
            </div>
            <div class="p-4">
              <div class="flex flex-wrap gap-3 mb-3">
                <div v-for="(val, key) in selectedStudent.enterpriseDetail.scores" :key="key" class="bg-[#F9FAFB] rounded-[6px] px-3 py-2 text-center">
                  <div class="text-[10px]" style="color:#9CA3AF;">{{ key }}</div>
                  <div class="text-[16px] font-semibold" style="color:#111827;">{{ val === '—' ? '—' : val }}</div>
                </div>
              </div>
              <p class="text-[13px] leading-relaxed" style="color:#374151;">{{ selectedStudent.enterpriseDetail.comment }}</p>
            </div>
          </div>

          <div v-if="!selectedStudent.teacherDetail && !selectedStudent.enterpriseDetail" class="text-center py-8 text-[14px]" style="color:#9CA3AF;">该生尚未评定</div>
        </div>
        <div class="p-[16px_24px] border-t border-[#E5E7EB] flex justify-end">
          <button class="h-[36px] px-[18px] rounded-[6px] text-[14px] cursor-pointer border border-[#D1D5DB] bg-white text-[#374151] hover:border-[#2563EB] hover:text-[#2563EB] transition-colors" @click="showDetail = false">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>
