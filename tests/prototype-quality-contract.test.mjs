import assert from 'node:assert/strict'
import { readFile, access } from 'node:fs/promises'
import { resolve, dirname } from 'node:path'
import { fileURLToPath } from 'node:url'
import test from 'node:test'

const __dirname = dirname(fileURLToPath(import.meta.url))
const loginSource = await readFile(new URL('../src/views/auth/Login.vue', import.meta.url), 'utf8')
const portalSource = await readFile(new URL('../src/views/ent/PortalLanding.vue', import.meta.url), 'utf8')
const entViews = [
  'Dashboard', 'ApplicationList', 'JobList', 'JobEdit', 'Profile', 'InternshipList',
]

// ── Task 1-4 regression contracts ──────────────────────────────

test('COM-AUTH-01 login page does not use fixed Figma left coordinates', () => {
  assert.doesNotMatch(loginSource, /left-\[641px\]/, 'logo should not use fixed Figma x=641')
  assert.doesNotMatch(loginSource, /left-\[613px\]/, 'title should not use fixed Figma x=613')
  assert.doesNotMatch(loginSource, /left-\[494px\]/, 'form should not use fixed Figma x=494')
})

test('COM-AUTH-01 login page uses centering layout', () => {
  assert.match(loginSource, /flex.*items-center|items-center.*flex|justify-center|mx-auto|place-items-center/, 'login content should be centered')
})

test('enterprise registration follows portal-intro path', () => {
  assert.match(loginSource, /to="\/portal"/, 'login "企业注册" link should go to /portal')
  assert.match(portalSource, /to="\/company\/register"/, 'portal CTA should go to /company/register')
})

test('ENT enterprise mentor sidebar links use router-link to /company/mentors', async () => {
  const src = await readFile(new URL('../src/components/CompanySidebar.vue', import.meta.url), 'utf8')
  assert.match(src, /\/company\/mentors/, 'CompanySidebar.vue should have mentor management link to /company/mentors')
})

test('ENT mentor page and route exist', async () => {
  const mentor = await readFile(new URL('../src/views/ent/MentorList.vue', import.meta.url), 'utf8')
  assert.match(mentor, /企业指导老师/, 'MentorList.vue should contain 企业指导老师')
  const router = await readFile(new URL('../src/router/ent.js', import.meta.url), 'utf8')
  assert.match(router, /\/company\/mentors/, 'ent.js router should register /company/mentors')
})

test('ENT role-specific enterprise pages and login entries exist', async () => {
  const router = await readFile(new URL('../src/router/ent.js', import.meta.url), 'utf8')
  for (const path of ['/company/admin', '/company/recruiter', '/company/contact', '/company/mentor']) {
    assert.match(router, new RegExp(path.replace(/\//g, '\\/')), `ent.js router should register ${path}`)
    assert.match(loginSource, new RegExp(path.replace(/\//g, '\\/')), `Login.vue should link to ${path}`)
  }
})

test('ENT admin can manage enterprise staff accounts', async () => {
  const router = await readFile(new URL('../src/router/ent.js', import.meta.url), 'utf8')
  const sidebar = await readFile(new URL('../src/components/CompanySidebar.vue', import.meta.url), 'utf8')
  const accountPage = await readFile(new URL('../src/views/ent/CompanyAccountManage.vue', import.meta.url), 'utf8')
  assert.match(router, /\/company\/admin\/accounts/, 'ent.js router should register /company/admin/accounts')
  assert.match(sidebar, /人员账号管理/, 'CompanySidebar should expose account management for admin')
  for (const text of ['企业联系人', '招聘负责人', '企业指导老师', '新增账号', '停用账号', '重置密码']) {
    assert.match(accountPage, new RegExp(text), `CompanyAccountManage.vue should include ${text}`)
  }
})

test('ENT job forms do not require internship type field', async () => {
  const jobListSource = await readFile(new URL('../src/views/ent/JobList.vue', import.meta.url), 'utf8')
  const jobEditSource = await readFile(new URL('../src/views/ent/JobEdit.vue', import.meta.url), 'utf8')
  assert.doesNotMatch(jobListSource, /实习类型.*<\/select>|实习类型.*<\/option>/, 'JobList inline form should not have internship type select')
  assert.doesNotMatch(jobEditSource, /实习类型.*<\/select>|实习类型.*<\/option>/, 'JobEdit form should not have internship type select')
})

// ── Task 6: unified auth page centering ────────────────────────

test('all auth pages use flexbox centering, not fixed Figma left coords', async () => {
  const authFiles = ['ForgotPassword', 'SSOAccount', 'SSOSms', 'SSOQr']
  for (const name of authFiles) {
    const src = await readFile(new URL(`../src/views/auth/${name}.vue`, import.meta.url), 'utf8')
    assert.doesNotMatch(src, /left-\[\d+px\]/, `${name}.vue should not use fixed Figma left-* coordinates`)
    assert.match(src, /flex.*items-center|items-center.*flex|justify-center/, `${name}.vue should use flexbox centering`)
  }
})

test('SSO pages provide return-to-login link', async () => {
  for (const name of ['SSOAccount', 'SSOSms', 'SSOQr']) {
    const src = await readFile(new URL(`../src/views/auth/${name}.vue`, import.meta.url), 'utf8')
    assert.match(src, /to="\/login"/, `${name}.vue should have a link back to /login`)
  }
})

test('no "待 Figma 设计还原" placeholder text remains in views', async () => {
  const colFiles = ['Dashboard', 'CompanyReview', 'NotificationManage']
  for (const name of colFiles) {
    const src = await readFile(new URL(`../src/views/col/${name}.vue`, import.meta.url), 'utf8')
    assert.doesNotMatch(src, /待 Figma 设计还原/, `col/${name}.vue should not contain placeholder text`)
  }
})

test('core router files reference existing Vue components', async () => {
  const routerFiles = ['auth', 'stu', 'tut', 'col', 'sch', 'ent', 'cou', 'sys']
  for (const rf of routerFiles) {
    const src = await readFile(new URL(`../src/router/${rf}.js`, import.meta.url), 'utf8')
    const matches = src.matchAll(/import\('(.+?)'\)/g)
    for (const m of matches) {
      const resolved = resolve(__dirname, '..', 'src', 'router', m[1])
      try { await access(resolved) } catch {
        assert.fail(`router/${rf}.js imports missing file: ${m[1]}`)
      }
    }
  }
  assert.ok(true, 'all route imports resolve to existing files')
})
