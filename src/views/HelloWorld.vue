<template>
  <div class="container">
    <h1>{{ text }}</h1>
    <button @click="callBackend" :disabled="loading">
      {{ loading ? 'Loading...' : 'Call Backend' }}
    </button>
    <div v-if="result" class="result">
      <pre>{{ JSON.stringify(result, null, 2) }}</pre>
    </div>
    <router-link to="/goodbye" class="link">Go to Goodbye Page</router-link>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const text = ref('Hello World')
const loading = ref(false)
const result = ref(null)

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

<style scoped>
.container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #0f0f23;
  color: #00ff88;
  gap: 30px;
  font-family: monospace;
}
h1 { font-size: 64px; }
button {
  padding: 12px 28px;
  font-size: 16px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  background: #0f3460;
  color: #fff;
}
button:disabled { opacity: 0.5; cursor: not-allowed; }
.result {
  padding: 16px 24px;
  background: #16213e;
  border-radius: 8px;
  font-size: 14px;
  color: #a0a0b0;
}
pre { color: #00ff88; margin: 0; }
.link {
  color: #e94560;
  text-decoration: none;
  font-size: 14px;
}
</style>
