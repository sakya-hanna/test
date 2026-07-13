<template>
  <div class="container">
    <h1>{{ text }}</h1>
    <p class="subtitle">A different style from Hello page</p>
    <button @click="callBackend" :disabled="loading">
      {{ loading ? 'Loading...' : 'Call Goodbye API' }}
    </button>
    <div v-if="result" class="result">
      <pre>{{ JSON.stringify(result, null, 2) }}</pre>
    </div>
    <router-link to="/" class="link">Back to Hello Page</router-link>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const text = ref('Goodbye World')
const loading = ref(false)
const result = ref(null)

async function callBackend() {
  loading.value = true
  result.value = null
  try {
    const res = await fetch('/api/goodbye', { method: 'POST' })
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
  background: linear-gradient(135deg, #1a1a2e, #16213e);
  color: #f5c518;
  gap: 24px;
  font-family: 'Georgia', serif;
}
h1 { font-size: 56px; font-style: italic; text-shadow: 2px 2px 4px rgba(0,0,0,0.5); }
.subtitle { color: #a0a0b0; font-size: 16px; }
button {
  padding: 14px 32px;
  font-size: 16px;
  border: 2px solid #f5c518;
  border-radius: 24px;
  cursor: pointer;
  background: transparent;
  color: #f5c518;
  transition: all 0.3s;
}
button:hover { background: #f5c518; color: #1a1a2e; }
button:disabled { opacity: 0.4; cursor: not-allowed; }
.result {
  padding: 20px 28px;
  background: rgba(0,0,0,0.3);
  border: 1px solid #f5c518;
  border-radius: 12px;
  font-size: 14px;
  color: #ccc;
}
pre { color: #f5c518; margin: 0; }
.link {
  color: #e94560;
  text-decoration: none;
  font-size: 14px;
  font-family: monospace;
}
</style>
