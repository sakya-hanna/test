import assert from 'node:assert/strict'
import { readFile } from 'node:fs/promises'
import test from 'node:test'

const source = await readFile(new URL('../src/views/stu/ParentNotice.vue', import.meta.url), 'utf8')

test('STU-PAR-01 presents the parent notice and receipt workflow', () => {
  assert.match(source, /页面编号：STU-PAR-01/)
  assert.match(source, /当前状态/)
  assert.match(source, /预览告知书/)
  assert.match(source, /下载告知书/)
  assert.match(source, /家长知情回执/)
  assert.match(source, /重新上传/)
  assert.match(source, /type="file"/)
  assert.doesNotMatch(source, /待从 Figma 设计还原/)
})

test('STU-PAR-01 keeps page interactions explicit', () => {
  assert.match(source, /togglePreview/)
  assert.match(source, /downloadNotice/)
  assert.match(source, /handleFileChange/)
  assert.match(source, /accept="\.pdf,\.jpg,\.jpeg,\.png"/)
})
