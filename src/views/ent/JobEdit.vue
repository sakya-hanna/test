<!-- Figma: 291:15 - ENT-JOB-02 - 岗位发布/编辑 -->
<template>
  <div class="flex min-h-screen">
    <CompanySidebar />

    <div class="flex-1 flex flex-col" style="background:#F5F7FB;">
      <header class="h-[72px] bg-white flex items-center justify-between px-6 shadow-sm flex-shrink-0">
        <span class="text-[14px]" style="color:#757575;">首页 / 实习需求管理 / 发布新岗位</span>
        <router-link to="/login" class="text-[14px] font-medium" style="color:#FF383C;">登出</router-link>
      </header>

      <main class="flex-1 overflow-auto p-6">
        <div class="mb-6">
          <p class="text-[14px]" style="color:#757575;">首页 / 实习需求管理 / 发布新岗位</p>
          <h2 class="text-[32px] font-bold text-gray-900 mt-1">发布新岗位</h2>
          <p class="text-[16px] mt-1" style="color:#757575;">填写岗位信息并发布，学生即可查看和申请</p>
        </div>

        <div class="bg-white rounded-2xl p-8 shadow-sm max-w-[800px] space-y-5">
          <div class="grid grid-cols-2 gap-5">
            <div class="col-span-2">
              <label class="block text-[13px] font-medium mb-1.5" style="color:#374151;">岗位名称 <span style="color:#EF4444;">*</span></label>
              <input v-model="f.name" placeholder="请输入岗位名称" class="w-full px-4 py-2.5 rounded-lg border text-[13px]" style="background:#F9FAFB;border-color:#D1D5DB;" />
            </div>
            <div>
              <label class="block text-[13px] font-medium mb-1.5" style="color:#374151;">招聘人数 <span style="color:#EF4444;">*</span></label>
              <input v-model="f.count" type="number" placeholder="请输入人数" class="w-full px-4 py-2.5 rounded-lg border text-[13px]" style="background:#F9FAFB;border-color:#D1D5DB;" />
            </div>
            <div>
              <label class="block text-[13px] font-medium mb-1.5" style="color:#374151;">工作地点 <span style="color:#EF4444;">*</span></label>
              <input v-model="f.loc" placeholder="请输入工作城市" class="w-full px-4 py-2.5 rounded-lg border text-[13px]" style="background:#F9FAFB;border-color:#D1D5DB;" />
            </div>
            <div>
              <label class="block text-[13px] font-medium mb-1.5" style="color:#374151;">学历要求</label>
              <select v-model="f.edu" class="w-full px-4 py-2.5 rounded-lg border text-[13px]" style="background:#F9FAFB;border-color:#D1D5DB;">
                <option value="">请选择</option>
                <option>本科及以上</option>
                <option>大专及以上</option>
                <option>不限</option>
              </select>
            </div>
            <div>
              <label class="block text-[13px] font-medium mb-1.5" style="color:#374151;">薪资范围</label>
              <input v-model="f.salary" placeholder="例如：3k-5k/月" class="w-full px-4 py-2.5 rounded-lg border text-[13px]" style="background:#F9FAFB;border-color:#D1D5DB;" />
            </div>
            <div class="col-span-2">
              <label class="block text-[13px] font-medium mb-1.5" style="color:#374151;">岗位职责</label>
              <textarea v-model="f.duties" rows="3" placeholder="请描述岗位职责" class="w-full px-4 py-2.5 rounded-lg border text-[13px] resize-none" style="background:#F9FAFB;border-color:#D1D5DB;"></textarea>
            </div>
            <div class="col-span-2">
              <label class="block text-[13px] font-medium mb-1.5" style="color:#374151;">任职要求</label>
              <textarea v-model="f.req" rows="3" placeholder="请描述任职要求" class="w-full px-4 py-2.5 rounded-lg border text-[13px] resize-none" style="background:#F9FAFB;border-color:#D1D5DB;"></textarea>
            </div>
          </div>

          <div class="flex gap-3 pt-4 border-t">
            <button @click="save" class="px-8 py-2.5 rounded-full text-[14px] font-medium text-white hover:opacity-90" style="background:#2563EB;">{{ ed ? '保存修改' : '发布岗位' }}</button>
            <router-link to="/company/jobs" class="px-8 py-2.5 rounded-full border text-[14px]" style="border-color:#D1D5DB;color:#6B7280;">取消</router-link>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import CompanySidebar from '../../components/CompanySidebar.vue'
import { reactive } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const ed = !!route.query.id
const f = reactive({
  name: ed ? 'Java开发工程师' : '',
  count: ed ? 3 : '',
  loc: ed ? '北京' : '',
  edu: '',
  salary: '',
  duties: '',
  req: '',
})

function save() {
  alert(ed ? '岗位已更新（演示）' : '岗位已发布（演示）')
}
</script>
