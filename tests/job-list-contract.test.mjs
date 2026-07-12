import assert from 'node:assert/strict'
import { readFile } from 'node:fs/promises'
import test from 'node:test'

const source = await readFile(new URL('../src/views/stu/JobList.vue', import.meta.url), 'utf8')

test('STU-JOB-01 follows the verified Figma list-page contract', () => {
  assert.match(source, /Figma Frame：164:2934/)
  assert.match(source, /搜索企业或岗位名称/)
  assert.match(source, /v-model="typeFilter"/)
  assert.match(source, /v-model="cityFilter"/)
  assert.match(source, /v-model="educationFilter"/)
  assert.match(source, /v-model="sortFilter"/)
  assert.match(source, /共找到 \{\{ filteredList\.length \}\} 个岗位/)
  assert.match(source, /截止:/)
  assert.match(source, /space-y-\[16px\]/)
  assert.doesNotMatch(source, /grid-cols-2/)
})

test('STU-JOB-01 keeps the application-chain navigation', () => {
  assert.match(source, /router\.push\('\/stu\/internships\/new'\)/)
  assert.match(source, /@click="apply\(job\.id\)"/)
})
