<!-- Figma: 217:16 - ENT-PROF-01 - 企业信息管理 -->
<template>
  <div class="flex min-h-screen">
    <!-- 侧边栏 -->
    <CompanySidebar />

    <div class="flex-1 flex flex-col" style="background:#F5F7FB;">
      <header class="h-[72px] bg-white flex items-center justify-between px-6 shadow-sm flex-shrink-0">
        <span class="text-[14px]" style="color:#757575;">企业管理 / 企业信息管理</span>
        <div class="flex items-center gap-4">
          <div class="text-right">
            <div class="text-[14px] font-medium" style="color:#000;">林负责人</div>
            <div class="text-[12px]" style="color:#DC2626;">企业管理员</div>
          </div>
          <router-link to="/login" class="text-[14px] font-medium" style="color:#FF383C;">登出</router-link>
        </div>
      </header>

      <main class="flex-1 overflow-auto p-6">
        <div class="flex justify-between items-start mb-6">
          <div>
            <span class="text-[14px]" style="color:#757575;">企业管理 / 企业信息管理</span>
            <h2 class="text-[32px] font-bold text-gray-900 mt-1">企业信息管理</h2>
            <p class="text-[16px] mt-1" style="color:#757575;">维护企业基础资料、联系方式与资质信息，提交后由学校审核认证</p>
          </div>
          <div class="flex gap-3">
            <button class="w-[114px] h-[36px] rounded-lg text-[14px] font-medium" style="border:1px solid #2563EB; color:#2563EB; background:#fff;">编辑资料</button>
            <button @click="submitReview" class="w-[120px] h-[36px] rounded-lg text-[14px] font-medium text-white hover:opacity-90" style="background:#2563EB;">提交审核</button>
          </div>
        </div>

        <!-- 企业基础信息卡片 -->
        <div class="bg-white rounded-2xl p-6 shadow-sm mb-6">
          <div class="flex justify-between items-center mb-4">
            <h3 class="text-[16px] font-bold" style="color:#000;">企业基础信息</h3>
            <span class="px-4 py-1.5 rounded-lg text-[12px] font-medium" style="background:rgba(22,163,74,0.16); color:#16A34A;">已认证</span>
          </div>
          <div class="grid grid-cols-2 gap-x-6 gap-y-4 text-[13px]">
            <div>
              <p class="mb-1.5 font-medium" style="color:#757575;">企业名称</p>
              <p style="color:#000;">帆软软件有限公司</p>
            </div>
            <div>
              <p class="mb-1.5 font-medium" style="color:#757575;">统一社会信用代码</p>
              <p style="color:#000;">91320200MA1234567X</p>
            </div>
            <div>
              <p class="mb-1.5 font-medium" style="color:#757575;">行业类型</p>
              <p style="color:#000;">软件和信息技术服务业</p>
            </div>
            <div>
              <p class="mb-1.5 font-medium" style="color:#757575;">企业规模</p>
              <p style="color:#000;">500-999 人</p>
            </div>
            <div>
              <p class="mb-1.5 font-medium" style="color:#757575;">联系人</p>
              <p style="color:#000;">陈志远</p>
            </div>
            <div>
              <p class="mb-1.5 font-medium" style="color:#757575;">联系电话</p>
              <p style="color:#000;">0510-88123456</p>
            </div>
            <div>
              <p class="mb-1.5 font-medium" style="color:#757575;">企业邮箱</p>
              <p style="color:#000;">hr@fanruan.com</p>
            </div>
            <div>
              <p class="mb-1.5 font-medium" style="color:#757575;">所在地区</p>
              <p style="color:#000;">江苏省 · 无锡市 · 滨湖区</p>
            </div>
            <div class="col-span-2">
              <p class="mb-1.5 font-medium" style="color:#757575;">企业地址</p>
              <p style="color:#000;">无锡市滨湖区菱湖大道 188 号软件园 A 栋 12 层</p>
            </div>
            <div class="col-span-2">
              <p class="mb-1.5 font-medium" style="color:#757575;">企业简介</p>
              <p style="color:#000;">帆软软件有限公司成立于 2006 年，是国内领先的商业智能与数据分析平台服务商，致力于为企业提供一站式的数据可视化与分析解决方案。</p>
            </div>
          </div>
        </div>

        <!-- 联系人表格 -->
        <div class="bg-white rounded-2xl shadow-sm overflow-hidden">
          <div class="px-6 py-4 flex justify-between items-center border-b">
            <h3 class="text-[16px] font-bold" style="color:#000;">联系人 / 分支机构信息</h3>
            <button class="w-[120px] h-[34px] rounded-lg text-[13px] font-medium text-white" style="background:#2563EB;">+ 新增联系人</button>
          </div>
          <table class="w-full text-[13px]">
            <thead><tr style="background:#F6F7F9;">
              <th class="text-left px-4 py-3 font-medium" style="color:#000;">序号</th>
              <th class="text-left px-4 py-3 font-medium" style="color:#000;">姓名</th>
              <th class="text-left px-4 py-3 font-medium" style="color:#000;">职位</th>
              <th class="text-left px-4 py-3 font-medium" style="color:#000;">联系电话</th>
              <th class="text-left px-4 py-3 font-medium" style="color:#000;">邮箱</th>
              <th class="text-left px-4 py-3 font-medium" style="color:#000;">状态</th>
              <th class="text-left px-4 py-3 font-medium" style="color:#000;">操作</th>
            </tr></thead>
            <tbody>
              <tr v-for="(c,i) in contacts" :key="c.id" class="border-b">
                <td class="px-4 py-3" style="color:#374151;">{{ i+1 }}</td>
                <td class="px-4 py-3" style="color:#000;">{{ c.name }}</td>
                <td class="px-4 py-3" style="color:#374151;">{{ c.title }}</td>
                <td class="px-4 py-3" style="color:#374151;">{{ c.phone }}</td>
                <td class="px-4 py-3" style="color:#374151;">{{ c.email }}</td>
                <td class="px-4 py-3"><span class="px-2 py-1 rounded text-[12px]" style="background:rgba(22,163,74,0.1); color:#16A34A;">{{ c.status }}</span></td>
                <td class="px-4 py-3">
                  <span @click="tip" class="cursor-pointer hover:underline" style="color:#2563EB;">编辑</span>
                  <span class="mx-2" style="color:#D1D5DB;">|</span>
                  <span @click="tip" class="cursor-pointer hover:underline" style="color:#EF4444;">删除</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import CompanySidebar from '../../components/CompanySidebar.vue'
function tip() { alert('原型展示，目标待确认') }
function submitReview() { alert('企业信息已提交审核（演示）') }
const contacts = [
  { id:1, name:'陈志远', title:'技术总监', phone:'0510-88123456', email:'chenzy@fanruan.com', status:'主联系人' },
  { id:2, name:'李明明', title:'HR经理', phone:'0510-88123457', email:'limm@fanruan.com', status:'普通联系人' },
  { id:3, name:'王建国', title:'研发主管', phone:'0510-88123458', email:'wangjg@fanruan.com', status:'企业导师' },
]
</script>
