<!-- Figma: 164:546 - ENT-JOB-01 - 实习需求管理 -->
<template>
  <div class="flex min-h-screen">
    <CompanySidebar />
    <div class="flex-1 flex flex-col" style="background:#F5F7FB;">
      <header class="h-[72px] bg-white flex items-center justify-between px-6 shadow-sm flex-shrink-0">
        <span class="text-[14px]" style="color:#757575;">首页 / 实习需求管理</span>
        <div class="flex items-center gap-4">
          <div class="flex items-center gap-2"><div class="w-10 h-10 rounded-full flex items-center justify-center text-xs font-medium" style="background:#C8C8C8;">王</div><div><div class="text-[14px] font-medium text-gray-900">王经理</div><div class="text-[12px]" style="color:#16A34A;">企业用户</div></div></div>
          <router-link to="/login" class="text-[14px] font-medium" style="color:#FF383C;">登出</router-link>
        </div>
      </header>
      <main class="flex-1 overflow-auto p-6">
        <div class="mb-6"><p class="text-[14px]" style="color:#757575;">首页 / 实习需求管理</p><h2 class="text-[32px] font-bold text-gray-900 mt-1">实习需求管理</h2><p class="text-[16px] mt-1" style="color:#757575;">发布、编辑和管理企业实习招聘岗位</p></div>
        <div class="flex gap-5 items-start">
          <!-- 左：发布新岗位 560px -->
          <div class="bg-white rounded-2xl p-8 shadow-sm flex-shrink-0" style="width:560px;">
            <h3 class="text-[18px] font-semibold text-gray-900 mb-6">发布新岗位</h3>
            <div class="space-y-4">
              <div><label class="block text-[13px] font-medium mb-1.5" style="color:#374151;">岗位名称</label><input v-model="f.name" placeholder="例如：Java开发工程师" class="w-full px-4 py-2.5 rounded-lg border text-[13px] focus:outline-none" style="background:#F9FAFB; border-color:#E5E7EB; color:#9CA3AF;" /></div>
              <div><label class="block text-[13px] font-medium mb-1.5" style="color:#374151;">需求人数</label><input v-model="f.count" placeholder="例如：3" class="w-full px-4 py-2.5 rounded-lg border text-[13px] focus:outline-none" style="background:#F9FAFB; border-color:#E5E7EB; color:#9CA3AF;" /></div>
              <div><label class="block text-[13px] font-medium mb-1.5" style="color:#374151;">专业要求</label><input v-model="f.major" placeholder="例如：计算机相关专业" class="w-full px-4 py-2.5 rounded-lg border text-[13px] focus:outline-none" style="background:#F9FAFB; border-color:#E5E7EB; color:#9CA3AF;" /></div>
              <div><label class="block text-[13px] font-medium mb-1.5" style="color:#374151;">工作地点</label><input v-model="f.loc" placeholder="例如：无锡市新吴区" class="w-full px-4 py-2.5 rounded-lg border text-[13px] focus:outline-none" style="background:#F9FAFB; border-color:#E5E7EB; color:#9CA3AF;" /></div>
              <div><label class="block text-[13px] font-medium mb-1.5" style="color:#374151;">岗位描述</label><textarea v-model="f.desc" rows="4" placeholder="请输入岗位职责和要求..." class="w-full px-4 py-2.5 rounded-lg border text-[13px] focus:outline-none resize-none" style="background:#F9FAFB; border-color:#E5E7EB; color:#9CA3AF;"></textarea></div>
              <button @click="pub" class="px-8 py-2.5 rounded-full text-[14px] font-medium text-white hover:opacity-90" style="background:#2563EB;">发布岗位</button>
            </div>
          </div>
          <!-- 右：已发布岗位 -->
          <div class="bg-white rounded-2xl p-8 shadow-sm flex-1">
            <h3 class="text-[18px] font-semibold text-gray-900 mb-6">已发布岗位</h3>
            <div class="space-y-3">
              <div v-for="j in list" :key="j.id" class="flex items-center rounded-lg px-4 py-3" style="background:#F9FAFB;">
                <div class="flex-1"><p class="text-[14px] font-semibold text-gray-900">{{ j.name }}</p><p class="text-[12px] mt-0.5" style="color:#6B7280;">{{ j.type }}  |  {{ j.count }}人  |  {{ j.apps }}份申请</p></div>
                <div class="flex-1 flex justify-center"><span class="inline-flex items-center rounded-md px-3 py-1 text-[11px] font-medium flex-shrink-0" style="min-width:64px;" :class="j.stat==='招聘中'?'text-[#16A34A]':j.stat==='已招满'?'text-[#FF383C]':'text-[#6B7280]'">{{ j.stat }}</span></div>
                <span @click="editJob(j)" class="text-[12px] font-medium cursor-pointer flex-shrink-0 text-center hover:underline" style="width:40px; color:#2563EB;">编辑</span>
                <span @click="toggleJob(j)" class="text-[12px] font-medium cursor-pointer flex-shrink-0 text-center hover:underline" style="width:40px;" :style="{color:j.stat==='已下架'?'#16A34A':'#FF383C'}">{{ j.stat==='已下架'?'上架':'下架' }}</span>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>
<script setup>
import CompanySidebar from '../../components/CompanySidebar.vue'
import { reactive, ref } from 'vue'
const f=reactive({name:'',count:'',major:'',loc:'',desc:''})
const list=ref([{id:1,name:'Java开发工程师',type:'毕业实习',count:3,apps:15,stat:'招聘中'},{id:2,name:'前端开发实习生',type:'毕业实习',count:2,apps:8,stat:'招聘中'},{id:3,name:'测试工程师',type:'灵活实习',count:1,apps:20,stat:'已招满'},{id:4,name:'数据分析实习生',type:'企业需求对接',count:2,apps:6,stat:'招聘中'},{id:5,name:'产品助理',type:'认知实习',count:1,apps:3,stat:'已下架'}])
function pub(){list.value.unshift({id:Date.now(),name:f.name,type:'企业发布',count:f.count,apps:0,stat:'招聘中'});Object.assign(f,{name:'',count:'',major:'',loc:'',desc:''})}
function editJob(j) { alert(`编辑岗位（演示）：${j.name}`) }
function toggleJob(j) { j.stat = j.stat === '已下架' ? '招聘中' : '已下架' }
</script>
