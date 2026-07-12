<template>
  <div class="flex min-h-screen">
    <!-- 侧边栏 -->
    <CompanySidebar />

    <!-- 右侧内容 -->
    <div class="flex-1 flex flex-col" style="background:#F5F7FB;">
      <header class="h-[72px] bg-white flex items-center justify-between px-6 shadow-sm flex-shrink-0">
        <span class="text-[14px]" style="color:#757575;">企业管理 / 企业指导老师</span>
        <div class="flex items-center gap-4">
          <div class="flex items-center gap-2">
            <div class="w-10 h-10 rounded-full flex items-center justify-center text-xs font-medium text-white" style="background:#2563EB;">华</div>
            <div>
              <div class="text-[14px] font-medium text-gray-900">华为技术有限公司</div>
              <div class="text-[12px]" style="color:#16A34A;">企业用户</div>
            </div>
          </div>
          <router-link to="/login" class="text-[14px] font-medium" style="color:#FF383C;">登出</router-link>
        </div>
      </header>

      <main class="flex-1 overflow-auto p-6">
        <!-- 标题区 -->
        <div class="mb-6">
          <p class="text-[14px]" style="color:#757575;">企业管理 / 企业指导老师</p>
          <h2 class="text-[32px] font-bold text-gray-900 mt-1">企业指导老师</h2>
          <p class="text-[16px] mt-1" style="color:#757575;">管理企业内指导老师及其分配的学生与评价进度</p>
        </div>

        <!-- 统计卡片 -->
        <div class="grid grid-cols-4 gap-5 mb-6">
          <div class="bg-white rounded-2xl p-5 shadow-sm">
            <p class="text-[13px] mb-2" style="color:#6B7280;">指导老师总数</p>
            <p class="text-[28px] font-bold" style="color:#2563EB;">{{ mentors.length }} 人</p>
          </div>
          <div class="bg-white rounded-2xl p-5 shadow-sm">
            <p class="text-[13px] mb-2" style="color:#6B7280;">指导学生总数</p>
            <p class="text-[28px] font-bold" style="color:#16A34A;">{{ totalStudents }} 人</p>
          </div>
          <div class="bg-white rounded-2xl p-5 shadow-sm">
            <p class="text-[13px] mb-2" style="color:#6B7280;">已完成评价</p>
            <p class="text-[28px] font-bold" style="color:#2563EB;">{{ doneCount }} 人</p>
          </div>
          <div class="bg-white rounded-2xl p-5 shadow-sm">
            <p class="text-[13px] mb-2" style="color:#6B7280;">待评价</p>
            <p class="text-[28px] font-bold" style="color:#D97706;">{{ pendingCount }} 人</p>
          </div>
        </div>

        <!-- 指导老师列表 -->
        <div class="bg-white rounded-2xl shadow-sm">
          <div class="flex items-center justify-between px-6 py-4 border-b border-gray-100">
            <h3 class="text-[18px] font-semibold text-gray-900">指导老师列表</h3>
            <button @click="showAdd = true" class="px-5 py-2 text-[13px] font-medium text-white rounded-[8px] hover:opacity-90" style="background:#2563EB;">+ 添加指导老师</button>
          </div>

          <!-- 表格 -->
          <div class="overflow-x-auto">
            <table class="w-full">
              <thead>
                <tr class="text-left text-[13px]" style="color:#6B7280; background:#F9FAFB;">
                  <th class="px-6 py-3 font-medium">姓名</th>
                  <th class="px-6 py-3 font-medium">部门/职位</th>
                  <th class="px-6 py-3 font-medium">手机号</th>
                  <th class="px-6 py-3 font-medium">指导学生</th>
                  <th class="px-6 py-3 font-medium">评价进度</th>
                  <th class="px-6 py-3 font-medium">操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="m in mentors" :key="m.id" class="border-t border-gray-50 hover:bg-gray-50/50 transition-colors">
                  <td class="px-6 py-4">
                    <div class="flex items-center gap-3">
                      <div class="w-9 h-9 rounded-full flex items-center justify-center text-[11px] font-medium text-white" :style="{background: m.avatarColor}">{{ m.name[0] }}</div>
                      <span class="text-[14px] font-medium text-gray-900">{{ m.name }}</span>
                    </div>
                  </td>
                  <td class="px-6 py-4 text-[13px]" style="color:#6B7280;">{{ m.dept }}</td>
                  <td class="px-6 py-4 text-[13px]" style="color:#6B7280;">{{ m.phone }}</td>
                  <td class="px-6 py-4">
                    <div class="flex flex-wrap gap-1">
                      <span v-for="s in m.students" :key="s.id" class="inline-flex items-center px-2 py-0.5 rounded-[4px] text-[11px] font-medium"
                        :style="{background: s.evaluated ? '#D1FAE5' : '#FEF3C7', color: s.evaluated ? '#065F46' : '#92400E'}">
                        {{ s.name }}
                      </span>
                    </div>
                  </td>
                  <td class="px-6 py-4">
                    <div class="flex items-center gap-2">
                      <div class="flex-1 h-2 rounded-full bg-gray-100 overflow-hidden" style="max-width:100px;">
                        <div class="h-full rounded-full transition-all" :style="{width: m.progress + '%', background: m.progress === 100 ? '#16A34A' : '#2563EB'}"></div>
                      </div>
                      <span class="text-[12px] font-medium" :style="{color: m.progress === 100 ? '#16A34A' : '#6B7280'}">{{ m.done }}/{{ m.students.length }}</span>
                    </div>
                  </td>
                  <td class="px-6 py-4">
                    <div class="flex items-center gap-2">
                      <button @click="showDetail(m)" class="text-[12px] font-medium cursor-pointer hover:underline" style="color:#2563EB;">查看详情</button>
                      <button @click="removeMentor(m.id)" class="text-[12px] font-medium cursor-pointer hover:underline" style="color:#FF383C;">移除</button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- 查看详情弹窗 -->
        <div v-if="detailMentor" class="fixed inset-0 z-50 flex items-center justify-center" style="background:rgba(0,0,0,0.3);" @click.self="detailMentor = null">
          <div class="bg-white rounded-2xl shadow-xl" style="width:800px; max-height:80vh; overflow-y:auto;">
            <div class="flex items-center justify-between px-8 py-6 border-b">
              <div>
                <h3 class="text-[18px] font-semibold text-gray-900">{{ detailMentor.name }} · 学生详情</h3>
                <p class="text-[13px] mt-1" style="color:#6B7280;">{{ detailMentor.dept }} | {{ detailMentor.phone }}</p>
              </div>
              <button @click="detailMentor = null" class="w-8 h-8 flex items-center justify-center rounded-full hover:bg-gray-100 text-[20px]" style="color:#6B7280;">×</button>
            </div>
            <div class="px-8 py-6">
              <table class="w-full">
                <thead>
                  <tr class="text-left text-[12px]" style="color:#6B7280;">
                    <th class="pb-3 pr-4 font-medium">学生姓名</th>
                    <th class="pb-3 pr-4 font-medium">学校</th>
                    <th class="pb-3 pr-4 font-medium">实习岗位</th>
                    <th class="pb-3 pr-4 font-medium">实习周期</th>
                    <th class="pb-3 pr-4 font-medium">评价状态</th>
                    <th class="pb-3 pr-4 font-medium">评分</th>
                    <th class="pb-3 pr-4 font-medium">操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="s in detailMentor.students" :key="s.id" class="border-t border-gray-100">
                    <td class="py-3 pr-4 text-[13px] font-medium text-gray-900">{{ s.name }}</td>
                    <td class="py-3 pr-4 text-[13px]" style="color:#6B7280;">{{ s.school }}</td>
                    <td class="py-3 pr-4 text-[13px]" style="color:#6B7280;">{{ s.position }}</td>
                    <td class="py-3 pr-4 text-[13px]" style="color:#6B7280;">{{ s.period }}</td>
                    <td class="py-3 pr-4">
                      <span class="inline-flex items-center px-2 py-0.5 rounded-[4px] text-[11px] font-medium"
                        :style="{background: s.evaluated ? '#D1FAE5' : '#FEF3C7', color: s.evaluated ? '#065F46' : '#92400E'}">
                        {{ s.evaluated ? '已完成' : '待评价' }}
                      </span>
                    </td>
                    <td class="py-3 pr-4">
                      <span v-if="s.evaluated" class="text-[13px] font-bold" :style="{color: '#2563EB'}">{{ s.score }} 分</span>
                      <span v-else class="text-[13px]" style="color:#9CA3AF;">—</span>
                    </td>
                    <td class="py-3 pr-4">
                      <button v-if="s.evaluated" @click="detailMentor = null; viewEvaluation(s)" class="text-[12px] font-medium cursor-pointer hover:underline" style="color:#2563EB;">查看评价</button>
                      <button v-else @click="remind(s)" class="text-[12px] font-medium cursor-pointer hover:underline" style="color:#D97706;">提醒评价</button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
            <div class="flex justify-end px-8 py-4 border-t">
              <button @click="detailMentor = null" class="px-6 py-2.5 text-[13px] rounded-[8px]" style="border:1px solid rgba(0,0,0,0.16); color:#6B7280; background:#fff;">关闭</button>
            </div>
          </div>
        </div>

        <!-- 添加指导老师弹窗 -->
        <div v-if="showAdd" class="fixed inset-0 z-50 flex items-center justify-center" style="background:rgba(0,0,0,0.3);" @click.self="showAdd = false">
          <div class="bg-white rounded-2xl p-8 shadow-xl" style="width:480px;">
            <h3 class="text-[18px] font-semibold text-gray-900 mb-6">添加企业指导老师</h3>
            <div class="space-y-4">
              <div>
                <label class="block text-[13px] mb-1.5" style="color:#374151;">姓名 <span style="color:#EF4444;">*</span></label>
                <input v-model="newMentor.name" placeholder="请输入姓名" class="w-full px-4 py-2.5 text-[13px] rounded-[8px] focus:outline-none" style="border:1px solid rgba(0,0,0,0.16);" />
              </div>
              <div>
                <label class="block text-[13px] mb-1.5" style="color:#374151;">手机号 <span style="color:#EF4444;">*</span></label>
                <input v-model="newMentor.phone" placeholder="请输入手机号" class="w-full px-4 py-2.5 text-[13px] rounded-[8px] focus:outline-none" style="border:1px solid rgba(0,0,0,0.16);" />
              </div>
              <div>
                <label class="block text-[13px] mb-1.5" style="color:#374151;">部门/职位</label>
                <input v-model="newMentor.dept" placeholder="请输入部门职位" class="w-full px-4 py-2.5 text-[13px] rounded-[8px] focus:outline-none" style="border:1px solid rgba(0,0,0,0.16);" />
              </div>
            </div>
            <div class="flex justify-end gap-3 mt-6">
              <button @click="showAdd = false" class="px-6 py-2.5 text-[13px] rounded-[8px]" style="border:1px solid rgba(0,0,0,0.16); color:#6B7280; background:#fff;">取消</button>
              <button @click="addMentor" class="px-6 py-2.5 text-[13px] font-medium text-white rounded-[8px] hover:opacity-90" style="background:#2563EB;">确认添加</button>
            </div>
          </div>
        </div>

        <!-- 查看评价弹窗 -->
        <div v-if="evalDetail" class="fixed inset-0 z-50 flex items-center justify-center" style="background:rgba(0,0,0,0.3);" @click.self="evalDetail = null">
          <div class="bg-white rounded-2xl p-8 shadow-xl" style="width:600px; max-height:80vh; overflow-y:auto;">
            <h3 class="text-[18px] font-semibold text-gray-900 mb-4">评价详情 — {{ evalDetail.name }}</h3>
            <div class="space-y-4">
              <div class="grid grid-cols-5 gap-3">
                <div v-for="d in evalDetail.details" :key="d.label" class="text-center p-3 rounded-[8px]" style="background:#F9FAFB;">
                  <p class="text-[12px] mb-1" style="color:#6B7280;">{{ d.label }}</p>
                  <p class="text-[18px] font-bold" style="color:#2563EB;">{{ d.score }}</p>
                </div>
              </div>
              <div class="p-4 rounded-[8px]" style="background:#F0FDF4;">
                <p class="text-[13px] font-medium" style="color:#065F46;">综合评分：{{ evalDetail.score }} 分</p>
              </div>
              <div v-for="c in evalDetail.comments" :key="c.label" class="mb-3">
                <p class="text-[13px] font-medium mb-1" style="color:#374151;">{{ c.label }}</p>
                <p class="text-[13px] p-3 rounded-[8px]" style="background:#F9FAFB; color:#6B7280;">{{ c.text }}</p>
              </div>
            </div>
            <div class="flex justify-end mt-6">
              <button @click="evalDetail = null" class="px-6 py-2.5 text-[13px] rounded-[8px]" style="border:1px solid rgba(0,0,0,0.16); color:#6B7280; background:#fff;">关闭</button>
            </div>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import CompanySidebar from '../../components/CompanySidebar.vue'
import { ref, computed, reactive } from 'vue'

const showAdd = ref(false)
const detailMentor = ref(null)
const evalDetail = ref(null)
const newMentor = reactive({ name: '', phone: '', dept: '' })

const mentors = ref([
  {
    id: 1, name: '陈志远', dept: '技术部 · 前端组长', phone: '138****6789', avatarColor: '#2563EB',
    students: [
      { id: 1, name: '王思远', school: '无锡学院', position: '前端开发工程师', period: '2026/03 - 2026/08', evaluated: true, score: 88, details: [{ label: '工作态度', score: 90 }, { label: '专业能力', score: 88 }, { label: '团队协作', score: 85 }, { label: '学习能力', score: 92 }, { label: '沟通表达', score: 86 }], comments: [{ label: '工作内容', text: '负责公司数据可视化产品前端组件库的开发与维护，参与核心报表模块的迭代与缺陷修复，按时高质量交付各期需求。' }, { label: '综合评语', text: '王思远同学实习期间表现优秀，技术能力强，工作态度端正，能够独立承担模块开发任务，获得团队一致好评。' }] },
      { id: 2, name: '李明', school: '无锡学院', position: '前端开发实习生', period: '2026/03 - 2026/08', evaluated: false, score: null },
    ]
  },
  {
    id: 2, name: '张丽华', dept: '产品部 · 高级产品经理', phone: '159****3210', avatarColor: '#7C3AED',
    students: [
      { id: 3, name: '赵强', school: '江南大学', position: '产品助理', period: '2026/04 - 2026/09', evaluated: true, score: 91, details: [{ label: '工作态度', score: 92 }, { label: '专业能力', score: 90 }, { label: '团队协作', score: 91 }, { label: '学习能力', score: 89 }, { label: '沟通表达', score: 93 }], comments: [{ label: '工作内容', text: '参与需求调研、PRD撰写和产品原型设计，独立完成3个功能模块的需求文档。' }, { label: '综合评语', text: '赵强同学逻辑清晰，沟通能力强，能快速理解业务需求并转化为可执行方案。' }] },
    ]
  },
  {
    id: 3, name: '刘国栋', dept: '测试部 · 测试主管', phone: '177****4567', avatarColor: '#059669',
    students: [
      { id: 4, name: '陈雪', school: '无锡学院', position: '测试工程师', period: '2026/03 - 2026/08', evaluated: false, score: null },
      { id: 5, name: '刘洋', school: '江南大学', position: '测试实习生', period: '2026/04 - 2026/09', evaluated: false, score: null },
    ]
  },
  {
    id: 4, name: '孙伟', dept: '后端组 · 高级Java工程师', phone: '186****7890', avatarColor: '#D97706',
    students: [
      { id: 6, name: '周杰', school: '无锡学院', position: 'Java开发实习生', period: '2026/03 - 2026/08', evaluated: true, score: 86, details: [{ label: '工作态度', score: 85 }, { label: '专业能力', score: 84 }, { label: '团队协作', score: 88 }, { label: '学习能力', score: 87 }, { label: '沟通表达', score: 86 }], comments: [{ label: '工作内容', text: '参与订单系统微服务开发，完成接口开发与单元测试，修复线上bug 4个。' }, { label: '综合评语', text: '周杰同学扎实的Java基础给团队留下深刻印象，代码质量较高，建议进一步加强系统设计能力。' }] },
    ]
  }
])

// Computed
const totalStudents = computed(() => mentors.value.reduce((sum, m) => sum + m.students.length, 0))
const doneCount = computed(() => mentors.value.reduce((sum, m) => sum + m.students.filter(s => s.evaluated).length, 0))
const pendingCount = computed(() => totalStudents.value - doneCount.value)

// Per-mentor computed stats
mentors.value.forEach(m => {
  m.done = m.students.filter(s => s.evaluated).length
  m.progress = m.students.length > 0 ? Math.round(m.done / m.students.length * 100) : 0
})

function showDetail(mentor) {
  detailMentor.value = mentor
}

function addMentor() {
  if (!newMentor.name || !newMentor.phone) return
  const colors = ['#2563EB', '#7C3AED', '#059669', '#D97706', '#DC2626', '#0891B2']
  mentors.value.push({
    id: Date.now(),
    name: newMentor.name,
    dept: newMentor.dept || '未设置',
    phone: newMentor.phone,
    avatarColor: colors[Math.floor(Math.random() * colors.length)],
    students: [],
    done: 0,
    progress: 0,
  })
  newMentor.name = ''
  newMentor.phone = ''
  newMentor.dept = ''
  showAdd.value = false
}

function removeMentor(id) {
  if (confirm('确定要移除该指导老师吗？')) {
    const idx = mentors.value.findIndex(m => m.id === id)
    if (idx > -1) mentors.value.splice(idx, 1)
    if (detailMentor.value?.id === id) detailMentor.value = null
  }
}

function viewEvaluation(student) {
  evalDetail.value = student
}

function remind(student) {
  alert('已向指导老师发送提醒通知（演示）')
}
</script>
