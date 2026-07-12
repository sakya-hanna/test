<!--
页面编号：COU-PAR-01
页面名称：家长知情确认
路由：/counselor/parent-notice
说明：管理学生家长的实习知情确认状态，发送确认请求，查看确认记录
-->
<script setup>
import { ref, computed } from 'vue'

const currentFilter = ref('全部')
const searchQuery = ref('')
const selectedRecord = ref(null)
const showDetail = ref(false)
const showSend = ref(false)

const records = ref([
  { id:1, name:'李明', sid:'2024030112', cls:'软件2201', parentName:'李父', parentPhone:'139-0001-5678', status:'已确认', statusBg:'rgba(22,163,74,0.1)', statusColor:'#16A34A', confirmTime:'2026-03-05 14:30', method:'短信链接', remark:'家长已阅读实习须知并确认同意' },
  { id:2, name:'王芳', sid:'2024030115', cls:'软件2202', parentName:'王母', parentPhone:'139-0002-5678', status:'已确认', statusBg:'rgba(22,163,74,0.1)', statusColor:'#16A34A', confirmTime:'2026-03-06 09:15', method:'微信扫码', remark:'家长已通过微信小程序确认' },
  { id:3, name:'赵强', sid:'2024030120', cls:'计算机2201', parentName:'赵父', parentPhone:'139-0003-5678', status:'待确认', statusBg:'rgba(217,119,6,0.1)', statusColor:'#D97706', confirmTime:'—', method:'短信链接（已发送）', remark:'已发送2次，家长尚未确认' },
  { id:4, name:'陈雪', sid:'2024030125', cls:'计算机2202', parentName:'陈母', parentPhone:'139-0004-5678', status:'已确认', statusBg:'rgba(22,163,74,0.1)', statusColor:'#16A34A', confirmTime:'2026-03-04 16:00', method:'辅导员电话确认', remark:'电话沟通确认，家长已知悉实习安排' },
  { id:5, name:'刘洋', sid:'2024030130', cls:'软件2201', parentName:'刘父', parentPhone:'139-0005-5678', status:'未发送', statusBg:'rgba(156,163,175,0.1)', statusColor:'#6B7280', confirmTime:'—', method:'—', remark:'—' },
  { id:6, name:'周婷', sid:'2024030135', cls:'计算机2201', parentName:'周父', parentPhone:'139-0006-5678', status:'已确认', statusBg:'rgba(22,163,74,0.1)', statusColor:'#16A34A', confirmTime:'2026-03-07 11:00', method:'短信链接', remark:'家长已确认并在线签署知情书' },
  { id:7, name:'吴磊', sid:'2024030140', cls:'软件2202', parentName:'吴母', parentPhone:'139-0007-5678', status:'待确认', statusBg:'rgba(217,119,6,0.1)', statusColor:'#D97706', confirmTime:'—', method:'短信链接（已发送）', remark:'已发送1次，3天后仍未确认需电话跟进' },
  { id:8, name:'郑欣', sid:'2024030145', cls:'计算机2201', parentName:'郑父', parentPhone:'139-0008-5678', status:'未发送', statusBg:'rgba(156,163,175,0.1)', statusColor:'#6B7280', confirmTime:'—', method:'—', remark:'—' },
])

const stats = computed(() => ({
  total: records.value.length,
  confirmed: records.value.filter(r => r.status === '已确认').length,
  pending: records.value.filter(r => r.status === '待确认').length,
  unsent: records.value.filter(r => r.status === '未发送').length,
}))

const filteredRecords = computed(() => {
  let list = records.value
  if (currentFilter.value === '已确认') list = list.filter(r => r.status === '已确认')
  if (currentFilter.value === '待确认') list = list.filter(r => r.status === '待确认')
  if (currentFilter.value === '未发送') list = list.filter(r => r.status === '未发送')
  if (searchQuery.value) {
    list = list.filter(r => r.name.includes(searchQuery.value) || r.sid.includes(searchQuery.value))
  }
  return list
})

function openDetail(record) {
  selectedRecord.value = record
  showDetail.value = true
}

function openSend(record) {
  selectedRecord.value = record
  showSend.value = true
}

function doSend() {
  showSend.value = false
  const r = records.value.find(x => x.id === selectedRecord.value.id)
  if (r) { r.status = '待确认'; r.statusBg = 'rgba(217,119,6,0.1)'; r.statusColor = '#D97706'; r.method = '短信链接（已发送）'; r.remark = '已发送确认请求，等待家长回复' }
}
</script>

<template>
  <div class="flex min-h-screen">
    <!-- 侧边栏 -->
    <aside class="w-[232px] min-h-screen flex-shrink-0 text-white flex flex-col" style="background:#00203D;">
      <div class="px-5 py-5 border-b border-white/10 text-lg font-semibold">实习管理系统</div>
      <nav class="flex-1 py-3 px-3 space-y-0.5">
        <router-link to="/counselor/dashboard" class="flex items-center px-4 py-3 rounded-lg text-[16px] text-white/80 hover:bg-white/10">指导工作台</router-link>
        <div>
          <router-link to="/counselor/student-list" class="flex items-center px-4 py-3 rounded-lg text-[16px] text-white/80 hover:bg-white/10">我的学生</router-link>
          <div class="ml-6 mt-0.5 space-y-0.5">
            <router-link to="/counselor/student-list" class="block text-[16px] text-white/80 px-2 py-1 rounded hover:bg-white/10">学生列表</router-link>
            <router-link to="/counselor/students" class="block text-[16px] text-white/80 px-2 py-1 rounded hover:bg-white/10">学生实习跟踪</router-link>
            <router-link to="/counselor/alerts" class="block text-[16px] text-white/80 px-2 py-1 rounded hover:bg-white/10">安全提醒沟通</router-link>
            <router-link to="/counselor/parent-notice" class="block text-[16px] px-2 py-1 rounded" style="color:#C7D2FE;">家长知情确认</router-link>
          </div>
        </div>
        <router-link to="/counselor/process-feedback" class="flex items-center px-4 py-3 rounded-lg text-[16px] text-white/80 hover:bg-white/10">过程反馈</router-link>
        <router-link to="/counselor/evaluations" class="flex items-center px-4 py-3 rounded-lg text-[16px] text-white/80 hover:bg-white/10">学生评价</router-link>
        <router-link to="/counselor/notifications" class="flex items-center px-4 py-3 rounded-lg text-[16px] text-white/80 hover:bg-white/10">通知公告</router-link>
      </nav>
    </aside>

    <!-- 右侧主体 -->
    <div class="flex-1 flex flex-col" style="background:#F5F7FB;">
      <header class="h-[72px] bg-white flex items-center justify-between px-6 shadow-sm flex-shrink-0">
        <span class="text-[14px]" style="color:#757575;">我的学生 / 家长知情确认</span>
        <div class="flex items-center gap-4">
          <div class="flex items-center gap-2">
            <div class="w-10 h-10 rounded-full flex items-center justify-center text-xs font-medium" style="background:#C8C8C8;">孙</div>
            <div>
              <div class="text-[14px] font-medium text-gray-900">孙辅导员</div>
              <div class="text-[12px]" style="color:#2563EB;">辅导员</div>
            </div>
          </div>
          <router-link to="/login" class="text-[14px] font-medium" style="color:#FF383C;">登出</router-link>
        </div>
      </header>

      <main class="flex-1 overflow-auto px-10 py-6">
        <!-- 页面标题 -->
        <div class="mb-5">
          <p class="text-[14px]" style="color:#757575;">我的学生 / 家长知情确认</p>
          <h2 class="text-[32px] font-bold text-gray-900 mt-1">家长知情确认</h2>
          <p class="text-[16px] mt-1" style="color:#757575;">向学生家长发送实习知情确认，跟踪确认状态，确保家校沟通到位</p>
        </div>

        <!-- 统计卡片 -->
        <div class="grid grid-cols-4 gap-4 mb-5">
          <div class="bg-white rounded-[12px] p-5 cursor-pointer" @click="currentFilter = '全部'">
            <p class="text-[13px] mb-2" style="color:#6B7280;">全部学生</p>
            <p class="text-[28px] font-bold" style="color:#2563EB;">{{ stats.total }}</p>
          </div>
          <div class="bg-white rounded-[12px] p-5 cursor-pointer" @click="currentFilter = '已确认'">
            <p class="text-[13px] mb-2" style="color:#6B7280;">已确认</p>
            <p class="text-[28px] font-bold" style="color:#16A34A;">{{ stats.confirmed }}</p>
          </div>
          <div class="bg-white rounded-[12px] p-5 cursor-pointer" @click="currentFilter = '待确认'">
            <p class="text-[13px] mb-2" style="color:#6B7280;">待确认</p>
            <p class="text-[28px] font-bold" style="color:#D97706;">{{ stats.pending }}</p>
          </div>
          <div class="bg-white rounded-[12px] p-5 cursor-pointer" @click="currentFilter = '未发送'">
            <p class="text-[13px] mb-2" style="color:#6B7280;">未发送</p>
            <p class="text-[28px] font-bold" style="color:#6B7280;">{{ stats.unsent }}</p>
          </div>
        </div>

        <!-- 筛选工具栏 -->
        <div class="flex items-center justify-between mb-5">
          <div class="flex items-center gap-3">
            <span class="text-[14px] font-medium text-gray-900">筛选：</span>
            <span v-for="f in ['全部','已确认','待确认','未发送']" :key="f"
              class="text-[14px] cursor-pointer px-2 py-1 rounded transition-colors"
              :class="currentFilter === f ? 'font-semibold' : ''"
              :style="{ color: currentFilter === f ? '#2563EB' : '#6B7280' }"
              @click="currentFilter = f"
            >{{ f }}</span>
          </div>
          <div class="flex items-center gap-3">
            <button class="h-[36px] px-[16px] bg-[#2563EB] text-white border-none rounded-[6px] text-[14px] cursor-pointer hover:bg-[#1D4ED8] transition-colors flex items-center gap-1" @click="openSend({id:0,name:'全部未确认学生',sid:'',cls:'',parentName:'',parentPhone:''})">
              📩 批量发送确认
            </button>
            <input v-model="searchQuery" type="text" placeholder="搜索学生姓名或学号..."
              class="w-[220px] h-[36px] rounded-[8px] border px-3 text-[14px] outline-none"
              style="border-color:rgba(0,0,0,0.16); color:#9CA3AF; background:white;" />
          </div>
        </div>

        <!-- 数据表格 -->
        <div class="bg-white rounded-[8px] shadow-sm mb-4 overflow-hidden">
          <div class="h-[44px] flex items-center px-4" style="background:#F3F4F6;">
            <div class="text-[12px] font-semibold flex-shrink-0" style="color:#374151; width:80px;">学生姓名</div>
            <div class="text-[12px] font-semibold flex-shrink-0" style="color:#374151; width:100px;">学号</div>
            <div class="text-[12px] font-semibold flex-shrink-0" style="color:#374151; width:100px;">班级</div>
            <div class="text-[12px] font-semibold flex-shrink-0" style="color:#374151; width:80px;">家长姓名</div>
            <div class="text-[12px] font-semibold flex-shrink-0" style="color:#374151; width:130px;">联系方式</div>
            <div class="text-[12px] font-semibold flex-shrink-0" style="color:#374151; width:140px;">确认方式</div>
            <div class="text-[12px] font-semibold flex-shrink-0" style="color:#374151; width:80px;">确认状态</div>
            <div class="text-[12px] font-semibold flex-shrink-0" style="color:#374151; width:120px;">确认时间</div>
            <div class="text-[12px] font-semibold flex-shrink-0" style="color:#374151; width:60px;">操作</div>
          </div>
          <div v-for="(r, i) in filteredRecords" :key="r.id">
            <div class="h-[52px] flex items-center px-4 hover:bg-[#F9FAFB] transition-colors">
              <div class="text-[12px] flex-shrink-0" style="color:#374151; width:80px;">{{ r.name }}</div>
              <div class="text-[12px] flex-shrink-0" style="color:#374151; width:100px;">{{ r.sid }}</div>
              <div class="text-[12px] flex-shrink-0" style="color:#374151; width:100px;">{{ r.cls }}</div>
              <div class="text-[12px] flex-shrink-0" style="color:#374151; width:80px;">{{ r.parentName }}</div>
              <div class="text-[12px] flex-shrink-0" style="color:#374151; width:130px;">{{ r.parentPhone }}</div>
              <div class="text-[12px] flex-shrink-0" style="color:#6B7280; width:140px;">{{ r.method }}</div>
              <div class="flex-shrink-0" style="width:80px;">
                <span class="inline-flex items-center rounded-[12px] px-3 py-1 text-[11px] font-medium" :style="{background:r.statusBg, color:r.statusColor}">{{ r.status }}</span>
              </div>
              <div class="text-[12px] flex-shrink-0" style="color:#6B7280; width:120px;">{{ r.confirmTime }}</div>
              <div class="flex-shrink-0 flex gap-2" style="width:60px;">
                <span class="text-[13px] font-medium cursor-pointer hover:underline" style="color:#2563EB;" @click="openDetail(r)">详情</span>
              </div>
            </div>
            <div v-if="i < filteredRecords.length - 1" class="h-px mx-4" style="background:#E5E7EB;"></div>
          </div>
          <div v-if="filteredRecords.length === 0" class="h-[120px] flex items-center justify-center text-[14px]" style="color:#9CA3AF;">暂无匹配的记录</div>
        </div>
      </main>
    </div>

    <!-- 详情弹窗 -->
    <div v-if="showDetail" class="fixed inset-0 z-50 flex items-center justify-center" style="background:rgba(0,0,0,0.45);" @click.self="showDetail = false">
      <div class="bg-white rounded-[12px] overflow-hidden" style="width:520px; max-height:80vh; overflow-y:auto;">
        <div class="p-[20px_24px] border-b border-[#E5E7EB] flex items-center justify-between">
          <span class="text-[18px] font-semibold text-[#111827]">确认详情 - {{ selectedRecord?.name }}</span>
          <button class="w-[28px] h-[28px] flex items-center justify-center rounded-[6px] text-[18px] text-[#6B7280] hover:bg-[#F3F4F6] border-none bg-none cursor-pointer" @click="showDetail = false">✕</button>
        </div>
        <div class="p-[24px]">
          <div class="grid grid-cols-2 gap-y-3 gap-x-5">
            <div class="flex gap-2 text-[13px]"><span style="color:#6B7280;">学生姓名：</span><span style="color:#111827;">{{ selectedRecord?.name }}</span></div>
            <div class="flex gap-2 text-[13px]"><span style="color:#6B7280;">学号：</span><span style="color:#111827;">{{ selectedRecord?.sid }}</span></div>
            <div class="flex gap-2 text-[13px]"><span style="color:#6B7280;">班级：</span><span style="color:#111827;">{{ selectedRecord?.cls }}</span></div>
            <div class="flex gap-2 text-[13px]"><span style="color:#6B7280;">家长姓名：</span><span style="color:#111827;">{{ selectedRecord?.parentName }}</span></div>
            <div class="flex gap-2 text-[13px]"><span style="color:#6B7280;">联系方式：</span><span style="color:#111827;">{{ selectedRecord?.parentPhone }}</span></div>
            <div class="flex gap-2 text-[13px]"><span style="color:#6B7280;">确认方式：</span><span style="color:#111827;">{{ selectedRecord?.method }}</span></div>
            <div class="flex gap-2 text-[13px]"><span style="color:#6B7280;">确认状态：</span><span :style="{color:selectedRecord?.statusColor}">{{ selectedRecord?.status }}</span></div>
            <div class="flex gap-2 text-[13px]"><span style="color:#6B7280;">确认时间：</span><span style="color:#111827;">{{ selectedRecord?.confirmTime }}</span></div>
          </div>
          <div class="mt-3 flex gap-2 text-[13px]"><span style="color:#6B7280;">备注：</span><span style="color:#111827;">{{ selectedRecord?.remark }}</span></div>
        </div>
        <div class="p-[16px_24px] border-t border-[#E5E7EB] flex justify-end gap-2">
          <button v-if="selectedRecord?.status !== '已确认'" class="h-[36px] px-[18px] rounded-[6px] text-[14px] cursor-pointer bg-[#2563EB] text-white border-none hover:bg-[#1D4ED8] transition-colors flex items-center gap-1"
            @click="showDetail = false; openSend(selectedRecord)">📩 发送确认</button>
          <button class="h-[36px] px-[18px] rounded-[6px] text-[14px] cursor-pointer border border-[#D1D5DB] bg-white text-[#374151] hover:border-[#2563EB] hover:text-[#2563EB] transition-colors" @click="showDetail = false">关闭</button>
        </div>
      </div>
    </div>

    <!-- 发送确认弹窗 -->
    <div v-if="showSend" class="fixed inset-0 z-50 flex items-center justify-center" style="background:rgba(0,0,0,0.45);" @click.self="showSend = false">
      <div class="bg-white rounded-[12px] overflow-hidden" style="width:480px;">
        <div class="p-[20px_24px] border-b border-[#E5E7EB] flex items-center justify-between">
          <span class="text-[18px] font-semibold text-[#111827]">发送家长知情确认</span>
          <button class="w-[28px] h-[28px] flex items-center justify-center rounded-[6px] text-[18px] text-[#6B7280] hover:bg-[#F3F4F6] border-none bg-none cursor-pointer" @click="showSend = false">✕</button>
        </div>
        <div class="p-[24px]">
          <div class="flex gap-2 text-[13px] mb-4">
            <span style="color:#6B7280;">发送对象家长：</span>
            <span style="color:#111827;">{{ selectedRecord?.name }} - {{ selectedRecord?.parentName }}（{{ selectedRecord?.parentPhone }}）</span>
          </div>
          <p class="text-[13px] mb-2" style="color:#6B7280;">发送渠道：</p>
          <div class="flex gap-4 mb-4">
            <label class="flex items-center gap-2 text-[13px] cursor-pointer"><input type="radio" name="channel" checked /> 短信链接</label>
            <label class="flex items-center gap-2 text-[13px] cursor-pointer"><input type="radio" name="channel" /> 微信通知</label>
          </div>
          <p class="text-[13px] mb-2" style="color:#6B7280;">确认内容：</p>
          <div class="bg-[#F9FAFB] p-3 rounded-[8px] text-[13px] text-[#374151] leading-relaxed mb-3">
            尊敬的{{ selectedRecord?.parentName || '家长' }}您好：<br><br>
            您的孩子{{ selectedRecord?.name || '同学' }}即将参加学校组织的实习活动。为确保家校沟通到位，请您阅读《实习安全须知》后点击确认。<br><br>
            实习期间请关注孩子的通勤安全和身心健康，如有异常请及时与辅导员联系。
          </div>
        </div>
        <div class="p-[16px_24px] border-t border-[#E5E7EB] flex justify-end gap-2">
          <button class="h-[36px] px-[18px] rounded-[6px] text-[14px] cursor-pointer border border-[#D1D5DB] bg-white text-[#374151] hover:border-[#2563EB] hover:text-[#2563EB] transition-colors" @click="showSend = false">取消</button>
          <button class="h-[36px] px-[18px] rounded-[6px] text-[14px] cursor-pointer bg-[#2563EB] text-white border-none hover:bg-[#1D4ED8] transition-colors" @click="doSend">确认发送</button>
        </div>
      </div>
    </div>
  </div>
</template>
