<!--
页面编号：COU-FB-01
页面名称：过程反馈
路由：/counselor/process-feedback
说明：辅导员监控学生实习过程中各方反馈的汇总视图
  覆盖：周报/月报提交进度 + 教师批阅反馈 + 企业评价反馈 + 辅导员反馈记录
-->
<script setup>
import { ref, computed } from 'vue'

const searchQuery = ref('')
const currentFilter = ref('全部')
const selectedStudent = ref(null)
const showDetail = ref(false)
const newFeedback = ref('')
const newFeedbackType = ref('observation')

const students = ref([
  { id:1, name:'李明', cls:'软件2201', type:'毕业实习', weekProgress:'9/12', teacherReview:'已批阅(良)', enterpriseReview:'已评价', lastFeedback:'教师批阅 · 07-02', status:'正常', statusBg:'rgba(22,163,74,0.1)', statusColor:'#16A34A',
    feedbacks:[
      { from:'指导老师 王老师', role:'教师', type:'周报批阅', content:'第9周周报内容详实，对项目进展描述清晰，技术总结到位。继续保持。', rating:'良好', time:'2026-07-02' },
      { from:'企业导师 陈志远', role:'企业', type:'月度评价', content:'该生工作态度积极，能够独立完成分配的任务，与团队配合良好。前端技能有明显提升。', rating:'优秀', time:'2026-06-28' },
      { from:'辅导员 孙老师', role:'辅导员', type:'日常观察', content:'与李明电话沟通，实习状态良好，对当前岗位满意度高。', rating:'—', time:'2026-06-20' },
    ]
  },
  { id:2, name:'王芳', cls:'软件2202', type:'企业需求对接', weekProgress:'8/12', teacherReview:'已批阅(优)', enterpriseReview:'待评价', lastFeedback:'教师批阅 · 07-01', status:'正常', statusBg:'rgba(22,163,74,0.1)', statusColor:'#16A34A',
    feedbacks:[
      { from:'指导老师 王老师', role:'教师', type:'周报批阅', content:'王芳同学本周完成了企业需求文档的整理工作，逻辑清晰，格式规范。', rating:'优秀', time:'2026-07-01' },
      { from:'辅导员 孙老师', role:'辅导员', type:'问题记录', content:'学生反映企业导师更换频繁，已建议学生主动与新导师沟通，关注后续适应情况。', rating:'—', time:'2026-06-25' },
    ]
  },
  { id:3, name:'赵强', cls:'计算机2201', type:'毕业实习', weekProgress:'7/12', teacherReview:'待批阅(3份)', enterpriseReview:'未评价', lastFeedback:'企业评价 · 06-15', status:'异常', statusBg:'rgba(255,56,60,0.1)', statusColor:'#FF383C',
    feedbacks:[
      { from:'企业导师 张工', role:'企业', type:'异常反馈', content:'赵强近两周出勤不稳定，已多次迟到，工作进度严重滞后。建议辅导员介入沟通。', rating:'差', time:'2026-06-15' },
      { from:'辅导员 孙老师', role:'辅导员', type:'沟通记录', content:'已与学生本人及家长电话沟通。学生表示身体不适，已建议其及时就医并补交假条。后续需持续关注。', rating:'—', time:'2026-06-16' },
    ]
  },
  { id:4, name:'陈雪', cls:'计算机2202', type:'灵活实习', weekProgress:'5/12', teacherReview:'已批阅(中)', enterpriseReview:'已评价', lastFeedback:'教师批阅 · 06-30', status:'需关注', statusBg:'rgba(217,119,6,0.1)', statusColor:'#D97706',
    feedbacks:[
      { from:'指导老师 李老师', role:'教师', type:'周报批阅', content:'周报内容较为简单，缺乏技术细节和独立思考。建议加强对实习内容的理解和总结。', rating:'中等', time:'2026-06-30' },
      { from:'企业导师 刘经理', role:'企业', type:'月度评价', content:'该生基础尚可但主动性不足，需要在指导下才能完成任务。建议多鼓励其独立思考和主动提问。', rating:'中等', time:'2026-06-22' },
    ]
  },
  { id:5, name:'刘洋', cls:'软件2201', type:'认知实习', weekProgress:'4/4', teacherReview:'已批阅(优)', enterpriseReview:'已评价', lastFeedback:'教师批阅 · 06-18', status:'已完成', statusBg:'rgba(107,114,128,0.1)', statusColor:'#6B7280',
    feedbacks:[
      { from:'指导老师 王老师', role:'教师', type:'实习总结批阅', content:'刘洋同学的认知实习报告质量很高，对企业技术栈的理解到位，对未来职业规划有清晰认识。', rating:'优秀', time:'2026-06-18' },
    ]
  },
  { id:6, name:'周婷', cls:'计算机2201', type:'毕业实习', weekProgress:'9/12', teacherReview:'已批阅(良)', enterpriseReview:'已评价', lastFeedback:'企业评价 · 07-01', status:'正常', statusBg:'rgba(22,163,74,0.1)', statusColor:'#16A34A',
    feedbacks:[
      { from:'企业导师 赵工', role:'企业', type:'月度评价', content:'周婷工作认真负责，代码质量较高，在团队中起到了积极的作用。', rating:'良好', time:'2026-07-01' },
    ]
  },
])

const stats = computed(() => ({
  total: students.value.length,
  normal: students.value.filter(s => s.status === '正常' || s.status === '已完成').length,
  pending: students.value.filter(s => s.teacherReview.includes('待批阅') || s.enterpriseReview.includes('未评价')).length,
  abnormal: students.value.filter(s => s.status === '异常' || s.status === '需关注').length,
}))

const filteredStudents = computed(() => {
  let list = students.value
  if (currentFilter.value === '正常') list = list.filter(s => s.status === '正常' || s.status === '已完成')
  if (currentFilter.value === '异常') list = list.filter(s => s.status === '异常' || s.status === '需关注')
  if (searchQuery.value) list = list.filter(s => s.name.includes(searchQuery.value) || s.cls.includes(searchQuery.value))
  return list
})

const feedbackTypes = [
  { key:'observation', label:'日常观察' },
  { key:'issue', label:'问题记录' },
  { key:'praise', label:'表扬' },
  { key:'suggest', label:'改进建议' },
]

function openDetail(s) { selectedStudent.value = s; showDetail.value = true; newFeedback.value = '' }
function addFeedback() {
  if (!newFeedback.value.trim() || !selectedStudent.value) return
  selectedStudent.value.feedbacks.unshift({
    from:'辅导员 孙老师', role:'辅导员', type: feedbackTypes.find(f => f.key === newFeedbackType.value)?.label || '日常观察',
    content: newFeedback.value.trim(), rating:'—', time: new Date().toISOString().slice(0,10),
  })
  newFeedback.value = ''
}
</script>

<template>
  <div class="flex min-h-screen">
    <!-- 侧边栏 -->
    <aside class="w-[232px] min-h-screen flex-shrink-0 text-white flex flex-col" style="background:#00203D;">
      <div class="px-5 py-5 border-b border-white/10 text-lg font-semibold">实习管理系统</div>
      <nav class="flex-1 py-3 px-3 space-y-0.5">
        <router-link to="/counselor/dashboard" class="flex items-center px-4 py-3 rounded-lg text-[16px] text-white/80 hover:bg-white/10">指导工作台</router-link>
        <router-link to="/counselor/student-list" class="flex items-center px-4 py-3 rounded-lg text-[16px] text-white/80 hover:bg-white/10">我的学生</router-link>
        <router-link to="/counselor/process-feedback" class="flex items-center px-4 py-3 rounded-lg text-[16px]" style="color:#C7D2FE;">过程反馈</router-link>
        <router-link to="/counselor/evaluations" class="flex items-center px-4 py-3 rounded-lg text-[16px] text-white/80 hover:bg-white/10">学生评价</router-link>
        <router-link to="/counselor/notifications" class="flex items-center px-4 py-3 rounded-lg text-[16px] text-white/80 hover:bg-white/10">通知公告</router-link>
      </nav>
    </aside>

    <!-- 右侧主体 -->
    <div class="flex-1 flex flex-col" style="background:#F5F7FB;">
      <header class="h-[72px] bg-white flex items-center justify-between px-6 shadow-sm flex-shrink-0">
        <span class="text-[14px]" style="color:#757575;">首页 / 过程反馈</span>
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
          <p class="text-[14px]" style="color:#757575;">首页 / 过程反馈</p>
          <h2 class="text-[32px] font-bold text-gray-900 mt-1">过程反馈</h2>
          <p class="text-[16px] mt-1" style="color:#757575;">汇总教师批阅、企业评价和辅导员反馈，全面掌握学生实习过程状态</p>
        </div>

        <!-- 统计卡片 -->
        <div class="grid grid-cols-4 gap-4 mb-5">
          <div class="bg-white rounded-[12px] p-5 cursor-pointer" @click="currentFilter = '全部'">
            <p class="text-[13px] mb-2" style="color:#6B7280;">全部学生</p>
            <p class="text-[28px] font-bold" style="color:#2563EB;">{{ stats.total }}</p>
          </div>
          <div class="bg-white rounded-[12px] p-5 cursor-pointer" @click="currentFilter = '正常'">
            <p class="text-[13px] mb-2" style="color:#6B7280;">反馈正常</p>
            <p class="text-[28px] font-bold" style="color:#16A34A;">{{ stats.normal }}</p>
          </div>
          <div class="bg-white rounded-[12px] p-5">
            <p class="text-[13px] mb-2" style="color:#6B7280;">待批阅/待评价</p>
            <p class="text-[28px] font-bold" style="color:#D97706;">{{ stats.pending }}</p>
          </div>
          <div class="bg-white rounded-[12px] p-5 cursor-pointer" @click="currentFilter = '异常'">
            <p class="text-[13px] mb-2" style="color:#6B7280;">需关注</p>
            <p class="text-[28px] font-bold" style="color:#FF383C;">{{ stats.abnormal }}</p>
          </div>
        </div>

        <!-- 筛选工具栏 -->
        <div class="flex items-center justify-between mb-5">
          <div class="flex items-center gap-3">
            <span class="text-[14px] font-medium text-gray-900">筛选：</span>
            <span v-for="f in ['全部','正常','异常']" :key="f"
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
            <div class="text-[12px] font-semibold flex-shrink-0" style="color:#374151; width:120px;">实习类型</div>
            <div class="text-[12px] font-semibold flex-shrink-0" style="color:#374151; width:100px;">周报进度</div>
            <div class="text-[12px] font-semibold flex-shrink-0" style="color:#374151; width:120px;">教师批阅</div>
            <div class="text-[12px] font-semibold flex-shrink-0" style="color:#374151; width:100px;">企业反馈</div>
            <div class="text-[12px] font-semibold flex-shrink-0" style="color:#374151; width:160px;">最近反馈</div>
            <div class="text-[12px] font-semibold flex-shrink-0" style="color:#374151; width:80px;">状态</div>
            <div class="text-[12px] font-semibold flex-shrink-0" style="color:#374151; width:60px;">操作</div>
          </div>
          <div v-for="(s, i) in filteredStudents" :key="s.id">
            <div class="h-[52px] flex items-center px-4 hover:bg-[#F9FAFB] transition-colors">
              <div class="text-[12px] flex-shrink-0" style="color:#374151; width:80px;">{{ s.name }}</div>
              <div class="text-[12px] flex-shrink-0" style="color:#374151; width:100px;">{{ s.cls }}</div>
              <div class="text-[12px] flex-shrink-0" style="color:#374151; width:120px;">{{ s.type }}</div>
              <div class="text-[12px] flex-shrink-0" style="color:#374151; width:100px;">{{ s.weekProgress }}</div>
              <div class="text-[12px] flex-shrink-0" style="color:#374151; width:120px;">{{ s.teacherReview }}</div>
              <div class="text-[12px] flex-shrink-0" style="color:#374151; width:100px;">{{ s.enterpriseReview }}</div>
              <div class="text-[12px] flex-shrink-0" style="color:#6B7280; width:160px;">{{ s.lastFeedback }}</div>
              <div class="flex-shrink-0" style="width:80px;">
                <span class="inline-flex items-center rounded-[12px] px-3 py-1 text-[11px] font-medium" :style="{background:s.statusBg, color:s.statusColor}">{{ s.status }}</span>
              </div>
              <div class="flex-shrink-0" style="width:60px;">
                <span class="text-[13px] font-medium cursor-pointer hover:underline" style="color:#2563EB;" @click="openDetail(s)">详情</span>
              </div>
            </div>
            <div v-if="i < filteredStudents.length - 1" class="h-px mx-4" style="background:#E5E7EB;"></div>
          </div>
          <div v-if="filteredStudents.length === 0" class="h-[120px] flex items-center justify-center text-[14px]" style="color:#9CA3AF;">暂无匹配的记录</div>
        </div>
      </main>
    </div>

    <!-- 反馈详情弹窗 -->
    <div v-if="showDetail" class="fixed inset-0 z-50 flex items-center justify-center" style="background:rgba(0,0,0,0.45);" @click.self="showDetail = false">
      <div class="bg-white rounded-[12px] overflow-hidden" style="width:620px; max-height:85vh; overflow-y:auto;">
        <div class="p-[20px_24px] border-b border-[#E5E7EB] flex items-center justify-between">
          <span class="text-[18px] font-semibold text-[#111827]">反馈详情 - {{ selectedStudent?.name }}</span>
          <button class="w-[28px] h-[28px] flex items-center justify-center rounded-[6px] text-[18px] text-[#6B7280] hover:bg-[#F3F4F6] border-none bg-none cursor-pointer" @click="showDetail = false">✕</button>
        </div>
        <div class="p-[24px]">
          <!-- 学生信息 -->
          <div class="grid grid-cols-4 gap-y-2 gap-x-4 mb-4 pb-4 border-b border-[#E5E7EB]">
            <div class="text-[12px]"><span style="color:#6B7280;">姓名：</span><span style="color:#111827;">{{ selectedStudent?.name }}</span></div>
            <div class="text-[12px]"><span style="color:#6B7280;">班级：</span><span style="color:#111827;">{{ selectedStudent?.cls }}</span></div>
            <div class="text-[12px]"><span style="color:#6B7280;">类型：</span><span style="color:#111827;">{{ selectedStudent?.type }}</span></div>
            <div class="text-[12px]"><span style="color:#6B7280;">进度：</span><span style="color:#111827;">{{ selectedStudent?.weekProgress }}</span></div>
          </div>
          <!-- 反馈时间线 -->
          <div class="text-[14px] font-semibold text-[#111827] mb-3">反馈记录</div>
          <div v-if="selectedStudent?.feedbacks.length === 0" class="text-[13px] text-center py-6" style="color:#9CA3AF;">暂无反馈记录</div>
          <div v-for="(fb, idx) in selectedStudent?.feedbacks" :key="idx" class="flex gap-3 pb-4 relative">
            <div v-if="idx < (selectedStudent?.feedbacks.length || 0) - 1" class="absolute left-[15px] top-[36px] bottom-0 w-[2px]" style="background:#E5E7EB;"></div>
            <div class="w-[32px] h-[32px] rounded-full flex-shrink-0 flex items-center justify-center text-[14px] text-white font-medium"
              :style="{ background: fb.role === '教师' ? '#2563EB' : fb.role === '企业' ? '#16A34A' : '#D97706' }">
              {{ fb.role === '教师' ? '师' : fb.role === '企业' ? '企' : '辅' }}
            </div>
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-2 mb-1">
                <span class="text-[13px] font-medium" style="color:#111827;">{{ fb.from }}</span>
                <span class="text-[11px] px-2 py-0.5 rounded-[3px]"
                  :style="{ background: fb.role === '教师' ? '#EFF6FF' : fb.role === '企业' ? '#F0FDF4' : '#FFF7ED',
                            color: fb.role === '教师' ? '#2563EB' : fb.role === '企业' ? '#16A34A' : '#D97706' }">
                  {{ fb.type }}
                </span>
                <span v-if="fb.rating !== '—'" class="text-[11px] ml-auto" style="color:#6B7280;">评级：{{ fb.rating }}</span>
              </div>
              <p class="text-[12px] leading-relaxed" style="color:#374151;">{{ fb.content }}</p>
              <p class="text-[11px] mt-1" style="color:#9CA3AF;">{{ fb.time }}</p>
            </div>
          </div>
          <!-- 添加反馈 -->
          <div class="mt-4 pt-4 border-t border-[#E5E7EB]">
            <p class="text-[13px] font-medium mb-2" style="color:#374151;">添加辅导员反馈：</p>
            <div class="flex gap-2 mb-2">
              <button v-for="ft in feedbackTypes" :key="ft.key"
                class="text-[12px] px-3 py-1 rounded-[14px] border cursor-pointer transition-colors"
                :class="newFeedbackType === ft.key ? 'border-[#2563EB]' : 'border-[#D1D5DB]'"
                :style="newFeedbackType === ft.key ? 'background:#EFF6FF; color:#2563EB;' : 'background:#fff; color:#6B7280;'"
                @click="newFeedbackType = ft.key"
              >{{ ft.label }}</button>
            </div>
            <textarea v-model="newFeedback" rows="3" placeholder="输入反馈内容..."
              class="w-full rounded-[8px] border px-3 py-2 text-[13px] resize-none outline-none mb-2"
              style="border-color:rgba(0,0,0,0.16); background:#F9FAFB; color:#374151;"
            ></textarea>
            <button class="h-[36px] px-[18px] bg-[#2563EB] text-white border-none rounded-[6px] text-[13px] cursor-pointer hover:bg-[#1D4ED8] transition-colors"
              @click="addFeedback">提交反馈</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
