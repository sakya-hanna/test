<template>
  <div class="flex min-h-screen">
    <CompanySidebar />
    <div class="flex-1 flex flex-col" style="background:#F5F7FB;">
      <header class="h-[72px] bg-white flex items-center justify-between px-6 shadow-sm flex-shrink-0">
        <span class="text-[14px]" style="color:#757575;">企业管理 / 人员账号管理</span>
        <div class="flex items-center gap-4">
          <div class="text-right">
            <div class="text-[14px] font-medium text-gray-900">林负责人</div>
            <div class="text-[12px]" style="color:#DC2626;">企业管理员</div>
          </div>
          <router-link to="/login" class="text-[14px] font-medium" style="color:#FF383C;">登出</router-link>
        </div>
      </header>

      <main class="flex-1 overflow-auto p-6">
        <div class="flex items-start justify-between mb-6">
          <div>
            <p class="text-[14px]" style="color:#757575;">企业管理 / 人员账号管理</p>
            <h2 class="text-[32px] font-bold text-gray-900 mt-1">人员账号管理</h2>
            <p class="text-[16px] mt-1" style="color:#757575;">统一维护企业联系人、招聘负责人、企业指导老师账号。</p>
          </div>
          <button
            @click="showAdd = true"
            class="w-[120px] h-[38px] rounded-lg text-[14px] font-medium text-white hover:opacity-90"
            style="background:#2563EB;"
          >
            + 新增账号
          </button>
        </div>

        <div class="grid grid-cols-4 gap-5 mb-6">
          <div v-for="item in stats" :key="item.label" class="bg-white rounded-2xl p-5 shadow-sm">
            <p class="text-[13px] mb-2" style="color:#6B7280;">{{ item.label }}</p>
            <p class="text-[28px] font-bold" :style="{ color: item.color }">{{ item.value }}</p>
          </div>
        </div>

        <div class="bg-white rounded-2xl shadow-sm overflow-hidden">
          <div class="px-6 py-4 border-b flex items-center justify-between">
            <h3 class="text-[18px] font-semibold text-gray-900">账号列表</h3>
            <div class="flex items-center gap-3">
              <select v-model="roleFilter" class="w-[160px] h-[34px] px-3 rounded-lg border text-[13px]" style="border-color:#D1D5DB;">
                <option value="">全部职责</option>
                <option v-for="role in roleOptions" :key="role.value" :value="role.value">{{ role.label }}</option>
              </select>
              <input v-model="keyword" placeholder="搜索姓名/账号" class="w-[180px] h-[34px] px-3 rounded-lg border text-[13px]" style="border-color:#D1D5DB;" />
            </div>
          </div>

          <table class="w-full text-[13px]">
            <thead>
              <tr style="background:#F9FAFB;">
                <th class="text-left px-4 py-3 font-medium" style="color:#6B7280;">姓名</th>
                <th class="text-left px-4 py-3 font-medium" style="color:#6B7280;">账号</th>
                <th class="text-left px-4 py-3 font-medium" style="color:#6B7280;">职责</th>
                <th class="text-left px-4 py-3 font-medium" style="color:#6B7280;">手机号</th>
                <th class="text-left px-4 py-3 font-medium" style="color:#6B7280;">状态</th>
                <th class="text-left px-4 py-3 font-medium" style="color:#6B7280;">最近登录</th>
                <th class="text-left px-4 py-3 font-medium" style="color:#6B7280;">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="account in filteredAccounts" :key="account.id" class="border-t border-gray-100">
                <td class="px-4 py-3 font-medium text-gray-900">{{ account.name }}</td>
                <td class="px-4 py-3" style="color:#374151;">{{ account.username }}</td>
                <td class="px-4 py-3">
                  <span class="px-2.5 py-1 rounded text-[12px] font-medium" :style="roleStyle(account.role)">
                    {{ roleLabel(account.role) }}
                  </span>
                </td>
                <td class="px-4 py-3" style="color:#374151;">{{ account.phone }}</td>
                <td class="px-4 py-3">
                  <span
                    class="px-2.5 py-1 rounded-full text-[12px] font-medium"
                    :style="account.enabled ? 'background:#D1FAE5;color:#16A34A;' : 'background:#FEE2E2;color:#DC2626;'"
                  >
                    {{ account.enabled ? '启用中' : '已停用' }}
                  </span>
                </td>
                <td class="px-4 py-3" style="color:#6B7280;">{{ account.lastLogin }}</td>
                <td class="px-4 py-3">
                  <div class="flex items-center gap-3">
                    <button @click="toggleAccount(account)" class="font-medium hover:underline" :style="{ color: account.enabled ? '#DC2626' : '#16A34A' }">
                      {{ account.enabled ? '停用账号' : '启用账号' }}
                    </button>
                    <button @click="resetPassword(account)" class="font-medium hover:underline" style="color:#2563EB;">重置密码</button>
                  </div>
                </td>
              </tr>
              <tr v-if="filteredAccounts.length === 0">
                <td colspan="7" class="px-4 py-10 text-center" style="color:#9CA3AF;">暂无匹配账号</td>
              </tr>
            </tbody>
          </table>
        </div>

        <div v-if="showAdd" class="fixed inset-0 z-50 flex items-center justify-center" style="background:rgba(0,0,0,0.3);" @click.self="closeAdd">
          <div class="bg-white rounded-2xl p-8 shadow-xl" style="width:520px;">
            <div class="flex items-center justify-between mb-6">
              <h3 class="text-[18px] font-semibold text-gray-900">新增人员账号</h3>
              <button @click="closeAdd" class="text-[20px]" style="color:#6B7280;">×</button>
            </div>

            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-[13px] mb-1.5" style="color:#374151;">姓名 <span style="color:#EF4444;">*</span></label>
                <input v-model="form.name" class="w-full px-4 py-2.5 rounded-lg border text-[13px]" style="border-color:#D1D5DB;" placeholder="请输入姓名" />
              </div>
              <div>
                <label class="block text-[13px] mb-1.5" style="color:#374151;">职责 <span style="color:#EF4444;">*</span></label>
                <select v-model="form.role" class="w-full px-4 py-2.5 rounded-lg border text-[13px]" style="border-color:#D1D5DB;">
                  <option v-for="role in roleOptions" :key="role.value" :value="role.value">{{ role.label }}</option>
                </select>
              </div>
              <div>
                <label class="block text-[13px] mb-1.5" style="color:#374151;">登录账号 <span style="color:#EF4444;">*</span></label>
                <input v-model="form.username" class="w-full px-4 py-2.5 rounded-lg border text-[13px]" style="border-color:#D1D5DB;" placeholder="例如：chenzy" />
              </div>
              <div>
                <label class="block text-[13px] mb-1.5" style="color:#374151;">手机号</label>
                <input v-model="form.phone" class="w-full px-4 py-2.5 rounded-lg border text-[13px]" style="border-color:#D1D5DB;" placeholder="请输入手机号" />
              </div>
              <div>
                <label class="block text-[13px] mb-1.5" style="color:#374151;">初始密码</label>
                <input v-model="form.password" class="w-full px-4 py-2.5 rounded-lg border text-[13px]" style="border-color:#D1D5DB;" />
              </div>
              <div>
                <label class="block text-[13px] mb-1.5" style="color:#374151;">账号状态</label>
                <select v-model="form.enabled" class="w-full px-4 py-2.5 rounded-lg border text-[13px]" style="border-color:#D1D5DB;">
                  <option :value="true">立即启用</option>
                  <option :value="false">暂不启用</option>
                </select>
              </div>
            </div>

            <div class="mt-5 rounded-lg px-4 py-3 text-[13px]" style="background:#F9FAFB;color:#6B7280;">
              新增账号后可将初始密码通过线下方式发送给对应人员。后端接入后应改为短信或邮件通知。
            </div>

            <div class="flex justify-end gap-3 mt-6">
              <button @click="closeAdd" class="px-6 py-2.5 rounded-lg text-[13px]" style="border:1px solid #D1D5DB;color:#6B7280;background:#fff;">取消</button>
              <button @click="addAccount" class="px-6 py-2.5 rounded-lg text-[13px] font-medium text-white hover:opacity-90" style="background:#2563EB;">确认新增</button>
            </div>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import CompanySidebar from '../../components/CompanySidebar.vue'

const roleOptions = [
  { label: '企业联系人', value: 'contact' },
  { label: '招聘负责人', value: 'recruiter' },
  { label: '企业指导老师', value: 'mentor' },
]

const roleColors = {
  contact: 'background:#DBEAFE;color:#2563EB;',
  recruiter: 'background:#FEF3C7;color:#D97706;',
  mentor: 'background:#D1FAE5;color:#16A34A;',
}

const accounts = ref([
  { id: 1, name: '李明明', username: 'limm', role: 'contact', phone: '138****1024', enabled: true, lastLogin: '2026-07-08 09:20' },
  { id: 2, name: '王经理', username: 'wangjl', role: 'recruiter', phone: '139****3301', enabled: true, lastLogin: '2026-07-08 14:12' },
  { id: 3, name: '陈志远', username: 'chenzy', role: 'mentor', phone: '136****8890', enabled: true, lastLogin: '2026-07-07 18:40' },
  { id: 4, name: '刘国栋', username: 'liugd', role: 'mentor', phone: '137****4501', enabled: false, lastLogin: '2026-06-30 11:05' },
])

const showAdd = ref(false)
const roleFilter = ref('')
const keyword = ref('')
const form = reactive({
  name: '',
  role: 'contact',
  username: '',
  phone: '',
  password: 'Aa123456',
  enabled: true,
})

const filteredAccounts = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  return accounts.value.filter(item => {
    const matchRole = !roleFilter.value || item.role === roleFilter.value
    const matchKeyword = !kw || item.name.toLowerCase().includes(kw) || item.username.toLowerCase().includes(kw)
    return matchRole && matchKeyword
  })
})

const stats = computed(() => [
  { label: '账号总数', value: `${accounts.value.length} 个`, color: '#2563EB' },
  { label: '企业联系人', value: `${countByRole('contact')} 人`, color: '#2563EB' },
  { label: '招聘负责人', value: `${countByRole('recruiter')} 人`, color: '#D97706' },
  { label: '企业指导老师', value: `${countByRole('mentor')} 人`, color: '#16A34A' },
])

function countByRole(role) {
  return accounts.value.filter(item => item.role === role).length
}

function roleLabel(role) {
  return roleOptions.find(item => item.value === role)?.label || role
}

function roleStyle(role) {
  return roleColors[role] || 'background:#F3F4F6;color:#6B7280;'
}

function resetForm() {
  Object.assign(form, {
    name: '',
    role: 'contact',
    username: '',
    phone: '',
    password: 'Aa123456',
    enabled: true,
  })
}

function closeAdd() {
  showAdd.value = false
  resetForm()
}

function addAccount() {
  if (!form.name || !form.username) {
    alert('请填写姓名和登录账号（演示）')
    return
  }
  accounts.value.unshift({
    id: Date.now(),
    name: form.name,
    username: form.username,
    role: form.role,
    phone: form.phone || '-',
    enabled: form.enabled,
    lastLogin: '尚未登录',
  })
  alert(`账号已新增，初始密码：${form.password}（演示）`)
  closeAdd()
}

function toggleAccount(account) {
  account.enabled = !account.enabled
  alert(`${account.name} 的账号已${account.enabled ? '启用' : '停用'}（演示）`)
}

function resetPassword(account) {
  alert(`${account.name} 的密码已重置为 Aa123456（演示）`)
}
</script>
