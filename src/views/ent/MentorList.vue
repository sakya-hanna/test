<!-- Enterprise Mentor Presentation Page -->
<template>
  <div class="flex min-h-screen">
    <!-- 侧边栏 -->
    <CompanySidebar />

    <!-- 主内容区 -->
    <div class="flex-1 flex flex-col" style="background:#F5F7FB;">
      <header class="h-[72px] bg-white flex items-center justify-between px-6 shadow-sm flex-shrink-0">
        <span class="text-[14px]" style="color:#757575;">首页 / 企业指导老师</span>
        <div class="flex items-center gap-4">
          <div class="flex items-center gap-2">
            <div class="w-10 h-10 rounded-full flex items-center justify-center text-xs font-medium" style="background:#C8C8C8;">王</div>
            <div>
              <div class="text-[14px] font-medium text-gray-900">王经理</div>
              <div class="text-[12px]" style="color:#16A34A;">企业用户</div>
            </div>
          </div>
          <router-link to="/login" class="text-[14px] font-medium" style="color:#FF383C;">登出</router-link>
        </div>
      </header>

      <main class="flex-1 p-8 overflow-auto">
        <!-- 页面标题 -->
        <div class="mb-8">
          <h1 class="text-[32px] font-bold text-black">企业指导老师</h1>
          <p class="text-[16px] mt-1" style="color:#757575;">企业方指导老师信息展示（原型展示，业务规则待确认）</p>
        </div>

        <!-- 指导老师列表 -->
        <div class="bg-white rounded-[15px] border border-[rgba(0,0,0,0.25)] p-6">
          <div class="flex items-center justify-between mb-6">
            <h2 class="text-[20px] font-semibold text-gray-900">指导老师名录</h2>
            <span class="text-[14px] px-3 py-1 rounded-full" style="color:#16A34A;background:rgba(20,141,61,0.15);">共 3 位老师</span>
          </div>

          <!-- 表格 -->
          <table class="w-full text-left">
            <thead>
              <tr class="border-b border-gray-200">
                <th class="py-3 px-4 text-[14px] font-medium text-gray-500">姓名</th>
                <th class="py-3 px-4 text-[14px] font-medium text-gray-500">部门</th>
                <th class="py-3 px-4 text-[14px] font-medium text-gray-500">职位</th>
                <th class="py-3 px-4 text-[14px] font-medium text-gray-500">联系电话</th>
                <th class="py-3 px-4 text-[14px] font-medium text-gray-500">指导方向</th>
                <th class="py-3 px-4 text-[14px] font-medium text-gray-500">状态</th>
              </tr>
            </thead>
            <tbody>
              <tr class="border-b border-gray-100 hover:bg-gray-50">
                <td class="py-3 px-4 text-[14px] font-medium text-gray-900">张工</td>
                <td class="py-3 px-4 text-[14px] text-gray-600">研发部</td>
                <td class="py-3 px-4 text-[14px] text-gray-600">高级工程师</td>
                <td class="py-3 px-4 text-[14px] text-gray-600">138****6789</td>
                <td class="py-3 px-4 text-[14px] text-gray-600">软件开发</td>
                <td class="py-3 px-4"><span class="text-[12px] px-2 py-1 rounded-full" style="color:#16A34A;background:rgba(20,141,61,0.25);">指导中</span></td>
              </tr>
              <tr class="border-b border-gray-100 hover:bg-gray-50">
                <td class="py-3 px-4 text-[14px] font-medium text-gray-900">李姐</td>
                <td class="py-3 px-4 text-[14px] text-gray-600">市场部</td>
                <td class="py-3 px-4 text-[14px] text-gray-600">市场总监</td>
                <td class="py-3 px-4 text-[14px] text-gray-600">139****8901</td>
                <td class="py-3 px-4 text-[14px] text-gray-600">市场营销</td>
                <td class="py-3 px-4"><span class="text-[12px] px-2 py-1 rounded-full" style="color:#16A34A;background:rgba(20,141,61,0.25);">指导中</span></td>
              </tr>
              <tr class="border-b border-gray-100 hover:bg-gray-50">
                <td class="py-3 px-4 text-[14px] font-medium text-gray-900">赵博</td>
                <td class="py-3 px-4 text-[14px] text-gray-600">技术部</td>
                <td class="py-3 px-4 text-[14px] text-gray-600">架构师</td>
                <td class="py-3 px-4 text-[14px] text-gray-600">137****3456</td>
                <td class="py-3 px-4 text-[14px] text-gray-600">系统架构</td>
                <td class="py-3 px-4"><span class="text-[12px] px-2 py-1 rounded-full" style="color:#6B7280;background:rgba(107,114,128,0.15);">空闲</span></td>
              </tr>
            </tbody>
          </table>

          <!-- 空态说明 -->
          <div class="mt-6 p-4 rounded-[8px] text-[13px] text-gray-500" style="background:#F9FAFB;">
            此页面为原型展示。企业指导老师的增删改、与学校对接等业务规则待产品确认后实现。
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import CompanySidebar from '../../components/CompanySidebar.vue'
</script>
