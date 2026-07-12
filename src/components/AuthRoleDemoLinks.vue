<template>
  <div class="w-full mt-4 pt-4 border-t border-white/20">
    <p class="text-[13px] text-white/80 mb-3">演示身份入口</p>
    <div class="grid grid-cols-3 gap-2">
      <router-link
        v-for="role in roleLinks"
        :key="role.label"
        :to="role.path"
        class="h-[34px] rounded-[8px] flex items-center justify-center text-[13px] font-medium text-white bg-white/10 border border-white/20 hover:bg-white/20 transition-colors"
      >
        {{ role.label }}
      </router-link>
    </div>
  </div>
</template>

<script setup>
const roleLinks = [
  { label: '学生', path: '/stu/dashboard' },
  { label: '教师', path: '/teacher/dashboard' },
  { label: '学院', path: '/college/dashboard' },
  { label: '学校', path: '/school/dashboard' },
  { label: '系统', path: '/admin/dashboard' },
  { label: '辅导员', path: '/counselor/dashboard' },
]
</script>
