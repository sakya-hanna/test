<template>
  <div style="min-height:100vh;display:flex;flex-direction:column;align-items:center;justify-content:center;background:#0f0f23;color:#00ff88;gap:30px;">
    <h1 style="font-size:64px;font-family:monospace;">{{ text }}</h1>

    <div style="display:flex;gap:16px;">
      <button @click="changeText" style="padding:12px 28px;font-size:16px;border:none;border-radius:8px;cursor:pointer;background:#e94560;color:#fff;">
        Change to Sakuya-hanna
      </button>

      <button @click="insertDb" :disabled="loading" style="padding:12px 28px;font-size:16px;border:none;border-radius:8px;cursor:pointer;background:#0f3460;color:#fff;">
        {{ loading ? 'Inserting...' : 'Insert to Database' }}
      </button>
    </div>

    <div v-if="result" style="margin-top:20px;padding:16px 24px;background:#16213e;border-radius:8px;font-family:monospace;font-size:14px;color:#a0a0b0;">
      <p>API Response:</p>
      <pre style="color:#00ff88;">{{ JSON.stringify(result, null, 2) }}</pre>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const text = ref('Hello World')
const loading = ref(false)
const result = ref(null)

const API = 'http://106.14.139.17:8080/api/ping'

function changeText() {
  text.value = 'Sakuya-hanna'
}

async function insertDb() {
  loading.value = true
  result.value = null
  try {
    const res = await fetch(API, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ content: 'Hello from frontend at ' + new Date().toISOString() })
    })
    const data = await res.json()
    result.value = data
  } catch (e) {
    result.value = { error: e.message }
  } finally {
    loading.value = false
  }
}
</script>
