<!-- Figma: 292:15 + 297:30 - ENT-APP-01 - 学生申请管理+弹窗 -->
<template>
  <div class="flex min-h-screen">
    <CompanySidebar />
    <div class="flex-1 flex flex-col" style="background:#F5F7FB;">
      <header class="h-[72px] bg-white flex items-center justify-between px-6 shadow-sm flex-shrink-0">
        <span class="text-[14px]" style="color:#757575;">首页 / 学生申请管理</span>
        <div class="flex items-center gap-4">
          <div class="text-right"><div class="text-[14px] font-semibold text-gray-900">华为技术有限公司</div><div class="text-[12px]" style="color:#757575;">企业用户</div></div>
          <router-link to="/login" class="text-[14px] font-semibold" style="color:#FF383C;">登出</router-link>
        </div>
      </header>
      <main class="flex-1 overflow-auto p-6">
        <div class="mb-6"><p class="text-[14px]" style="color:#757575;">首页 / 学生申请管理</p><h2 class="text-[32px] font-bold text-gray-900 mt-1">学生申请管理</h2><p class="text-[16px] mt-1" style="color:#757575;">查看和管理学生对岗位的申请</p></div>
        <!-- 筛选栏 r=15 stroke=#000 -->
        <div class="bg-white px-6 py-4 mb-6 flex items-center gap-4 flex-wrap" style="border:1px solid rgba(0,0,0,0.1); border-radius:15px;">
          <div><label class="block text-[13px] mb-1.5" style="color:#666;">申请岗位</label><select v-model="flt.job" class="w-[200px] px-3 py-2 text-[13px] focus:outline-none" style="border:1px solid rgba(0,0,0,0.16); border-radius:8px; color:#999;"><option value="">全部岗位 ▼</option><option v-for="j in allJobs" :key="j" :value="j">{{ j }}</option></select></div>
          <div><label class="block text-[13px] mb-1.5" style="color:#666;">状态筛选</label><select v-model="flt.st" class="w-[80px] px-3 py-2 text-[13px] focus:outline-none" style="border:1px solid rgba(0,0,0,0.16); border-radius:8px; color:#999;"><option value="">全部</option><option value="待处理">待处理</option><option value="已通过">已通过</option><option value="已拒绝">已拒绝</option></select></div>
          <div><label class="block text-[13px] mb-1.5" style="color:#666;">搜索学生</label><input v-model="flt.kw" placeholder="输入学生姓名" class="w-[200px] px-3 py-2 text-[13px] focus:outline-none" style="border:1px solid rgba(0,0,0,0.16); border-radius:8px; color:#333;" /></div>
          <button class="w-[72px] h-[32px] text-[13px] font-semibold text-white" style="background:#2563EB; border-radius:16px;">查询</button>
          <button @click="flt.job='';flt.st='';flt.kw=''" class="w-[72px] h-[32px] text-[13px]" style="border:1px solid rgba(0,0,0,0.16); border-radius:16px; color:#666; background:#fff;">重置</button>
        </div>
        <!-- 数据表格 r=15 stroke=#000 -->
        <div style="border:1px solid rgba(0,0,0,0.1); border-radius:15px; overflow:hidden;">
          <table class="w-full text-[13px] border-collapse bg-white">
            <thead><tr style="background:#F5F7FB;"><th class="text-left px-3 py-3 font-semibold" style="color:#333;">序号</th><th class="text-left px-3 py-3 font-semibold" style="color:#333;">学生姓名</th><th class="text-left px-3 py-3 font-semibold" style="color:#333;">学校</th><th class="text-left px-3 py-3 font-semibold" style="color:#333;">专业</th><th class="text-left px-3 py-3 font-semibold" style="color:#333;">年级</th><th class="text-left px-3 py-3 font-semibold" style="color:#333;">申请岗位</th><th class="text-left px-3 py-3 font-semibold" style="color:#333;">申请时间</th><th class="text-left px-3 py-3 font-semibold" style="color:#333;">状态</th><th class="text-left px-3 py-3 font-semibold" style="color:#333;">操作</th></tr></thead>
            <tbody>
              <tr v-for="(a,i) in apps" :key="a.id" :style="{background:i%2===0?'#fff':'#FAFAFC'}">
                <td class="px-3 py-3" style="color:#333;">{{ i+1 }}</td><td class="px-3 py-3" style="color:#333;">{{ a.name }}</td><td class="px-3 py-3" style="color:#333;">{{ a.school }}</td><td class="px-3 py-3" style="color:#333;">{{ a.major }}</td><td class="px-3 py-3" style="color:#333;">{{ a.grade }}</td><td class="px-3 py-3" style="color:#333;">{{ a.job }}</td><td class="px-3 py-3" style="color:#333;">{{ a.time }}</td>
                <td class="px-3 py-3"><span class="inline-flex items-center px-3 py-1 text-[12px] font-semibold" style="border-radius:13px;" :style="ss(a.st)">{{ a.st }}</span></td>
                <td class="px-3 py-3"><button @click="sel=a" class="text-[13px]" style="color:#2563EB;">查看详情</button></td>
              </tr>
            </tbody>
          </table>
          <div class="px-6 py-3 text-center text-[13px] bg-white" style="color:#808080;">第 1/8 页，共 {{ apps.length }} 条</div>
        </div>
      </main>
    </div>
    <!-- Modal 620x680 -->
    <div v-if="sel" class="fixed inset-0 z-50 flex items-center justify-center" style="background:rgba(0,0,0,0.4);" @click.self="sel=null">
      <div class="bg-white overflow-auto" style="width:620px; max-height:90vh; border-radius:15px;">
        <div class="p-8">
          <div class="flex justify-between items-center mb-4"><h3 class="text-[22px] font-bold text-gray-900">学生申请详情</h3><button @click="sel=null" class="text-[20px]" style="color:#808080;">✕</button></div>
          <hr class="mb-5" style="border-color:#E6E6E6;">
          <h4 class="text-[16px] font-bold text-gray-900 mb-3">学生基本信息</h4>
          <div class="grid grid-cols-2 gap-y-3 gap-x-6 text-[13px] mb-5">
            <div><span style="color:#808080;">姓名</span><span class="ml-3 text-[14px] font-semibold" style="color:#1A1A1A;">{{ sel.name }}</span></div>
            <div><span style="color:#808080;">学号</span><span class="ml-3 text-[14px] font-semibold" style="color:#1A1A1A;">{{ sel.sid }}</span></div>
            <div><span style="color:#808080;">学校</span><span class="ml-3 text-[14px] font-semibold" style="color:#1A1A1A;">{{ sel.school }}</span></div>
            <div><span style="color:#808080;">院系</span><span class="ml-3 text-[14px] font-semibold" style="color:#1A1A1A;">{{ sel.dept }}</span></div>
            <div><span style="color:#808080;">专业</span><span class="ml-3 text-[14px] font-semibold" style="color:#1A1A1A;">{{ sel.major }}</span></div>
            <div><span style="color:#808080;">年级</span><span class="ml-3 text-[14px] font-semibold" style="color:#1A1A1A;">{{ sel.grade }}（{{ sel.gy }}级）</span></div>
          </div>
          <h4 class="text-[16px] font-bold text-gray-900 mb-3">申请信息</h4>
          <div class="grid grid-cols-2 gap-y-3 gap-x-6 text-[13px] mb-5">
            <div><span style="color:#808080;">申请岗位</span><span class="ml-3 text-[14px] font-semibold" style="color:#1A1A1A;">{{ sel.job }}</span></div>
            <div><span style="color:#808080;">申请时间</span><span class="ml-3 text-[14px] font-semibold" style="color:#1A1A1A;">{{ sel.ftime }}</span></div>
          </div>
          <h4 class="text-[16px] font-bold text-gray-900 mb-2">自我介绍</h4>
          <div class="p-4 text-[13px] mb-5 leading-relaxed" style="background:#F5F7FB; border-radius:8px; color:#4D4D4D;">{{ sel.intro }}</div>
          <h4 class="text-[16px] font-bold text-gray-900 mb-2">简历附件</h4>
          <div class="inline-flex items-center gap-2 px-5 py-2 text-[13px] mb-6" style="border:1px solid #2563EB; border-radius:24px; color:#2563EB;">📄 个人简历.pdf</div>
          <div class="flex gap-3 pt-4">
            <button class="w-[120px] h-[40px] text-[14px] font-semibold" style="border:1px solid #2563EB; border-radius:24px; color:#2563EB; background:#fff;">联系学生</button>
            <button class="w-[100px] h-[40px] text-[14px] font-semibold text-white" style="background:#FF383C; border-radius:24px;">拒绝</button>
            <button class="w-[100px] h-[40px] text-[14px] font-semibold text-white" style="background:#2563EB; border-radius:24px;">通过</button>
            <button @click="sel=null" class="text-[14px]" style="color:#6B7280;">取消</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script setup>
import CompanySidebar from '../../components/CompanySidebar.vue'
import { ref, reactive } from 'vue'
const flt=reactive({job:'',st:'',kw:''})
const allJobs=['Java开发工程师','前端开发实习生','后端开发实习生','测试工程师','数据分析实习生','算法实习生']
const apps=ref([{id:1,name:'张三',school:'无锡学院',major:'物联网工程',grade:'大三',gy:'2023',job:'前端开发实习生',time:'2026-06-10',ftime:'2026-06-10 15:30',st:'待处理',sid:'20231001001',dept:'物联网工程学院',intro:'我是物联网工程专业大三学生，对前端开发有浓厚兴趣。在校期间参与过校级项目开发，熟悉 React 和 Vue 框架。希望能在贵公司获得实习机会...'},{id:2,name:'李四',school:'无锡学院',major:'计算机科学',grade:'大四',gy:'2022',job:'后端开发实习生',time:'2026-06-12',ftime:'2026-06-12 10:00',st:'已通过',sid:'20221001002',dept:'计算机学院',intro:'计算机科学专业大四学生...'},{id:3,name:'王五',school:'无锡学院',major:'软件工程',grade:'大三',gy:'2023',job:'数据分析实习生',time:'2026-06-14',ftime:'2026-06-14 09:20',st:'已拒绝',sid:'20231001003',dept:'软件工程学院',intro:'软件工程专业...'},{id:4,name:'赵六',school:'无锡学院',major:'人工智能',grade:'大四',gy:'2022',job:'算法实习生',time:'2026-06-15',ftime:'2026-06-15 14:00',st:'待处理',sid:'20221001004',dept:'人工智能学院',intro:'人工智能专业大四学生...'},{id:5,name:'孙七',school:'无锡学院',major:'网络工程',grade:'大三',gy:'2023',job:'前端开发实习生',time:'2026-06-16',ftime:'2026-06-16 11:10',st:'待处理',sid:'20231001005',dept:'网络工程学院',intro:'网络工程专业...'}])
const sel=ref(null)
function ss(s){if(s==='待处理')return 'background:#FEF3C7; color:#CA8A04;';if(s==='已通过')return 'background:#D1FAE5; color:#16A34A;';if(s==='已拒绝')return 'background:#FEE2E2; color:#FF383C;';return''}
</script>
