<script setup>
import { ref } from 'vue'

const count = ref(0)
</script>

<template>
  <div style="text-align:center;padding:80px 20px;">
    <h1 style="color:#42b883;font-size:48px;">Auto Deploy Works!</h1>
    <p style="font-size:20px;color:#666;margin:20px 0;">
      自动部署成功 - {{ new Date().toLocaleString('zh-CN') }}
    </p>
    <button @click="count++" style="padding:12px 32px;font-size:18px;border-radius:8px;cursor:pointer;">
      Auto Deployed Successfully! Count: {{ count }}
    </button>
  </div>
</template>
