<template>
  <div style="min-height:100vh;display:flex;flex-direction:column;align-items:center;justify-content:center;background:#0f0f23;color:#00ff88;gap:30px;">
    <h1 style="font-size:64px;font-family:monospace;">{{ text }}</h1>

    <div style="display:flex;gap:16px;">
      <button @click="changeText" style="padding:12px 28px;font-size:16px;border:none;border-radius:8px;cursor:pointer;background:#e94560;color:#fff;">
        Change to Sakuya-hanna
      </button>

      <button @click="callBackend" :disabled="loading" style="padding:12px 28px;font-size:16px;border:none;border-radius:8px;cursor:pointer;background:#0f3460;color:#fff;">
        {{ loading ? 'Loading...' : 'Call Backend API' }}
      </button>
    </div>

    <div v-if="result" style="margin-top:20px;padding:16px 24px;background:#16213e;border-radius:8px;font-family:monospace;font-size:14px;color:#a0a0b0;">
      <pre style="color:#00ff88;">{{ JSON.stringify(result, null, 2) }}</pre>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const text = ref('Hello World')
const loading = ref(false)
const result = ref(null)

function changeText() {
  text.value = 'Sakuya-hanna'
}

async function callBackend() {
  loading.value = true
  result.value = null
  try {
    const res = await fetch('/api/ping', { method: 'POST' })
    result.value = await res.json()
  } catch (e) {
    result.value = { error: e.message }
  } finally {
    loading.value = false
  }
}
</script>
