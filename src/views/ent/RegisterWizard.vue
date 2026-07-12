<!-- Figma: ENT-ONB-01~04 275:15~278:15 - 企业注册四步向导 -->
<template>
  <div class="min-h-screen flex flex-col" style="background:#F5F7FB;">
    <!-- Toast -->
    <Transition name="toast-fade">
      <div v-if="toast.show" class="fixed top-6 left-1/2 -translate-x-1/2 z-50 px-6 py-3 rounded-[8px] text-[14px] text-white shadow-lg" style="background:rgba(0,0,0,0.78);">{{ toast.text }}</div>
    </Transition>

    <!-- 顶栏 -->
    <header class="h-16 bg-white flex items-center justify-between px-[60px] shadow-sm flex-shrink-0">
      <div class="flex items-center gap-3">
        <span class="text-[20px] font-bold" style="color:#2563EB;">无锡学院</span>
        <span class="text-[13px]" style="color:#757575;">实习管理系统</span>
      </div>
      <router-link to="/login" class="text-[14px]" style="color:#2563EB;">已有账号？立即登录</router-link>
    </header>

    <!-- Stepper -->
    <div class="flex justify-center py-8 bg-white border-b">
      <div class="flex items-center">
        <div v-for="(s,i) in steps" :key="i" class="flex items-center">
          <div class="flex flex-col items-center">
            <div class="w-10 h-10 rounded-full flex items-center justify-center text-[16px] font-bold"
              :style="{background:(step>i+1||step===i+1)?'#2563EB':'#D1D5DB', color:'#fff'}">
              {{ step>i+1?'✓':i+1 }}
            </div>
            <span class="text-[13px] mt-2" :style="{color:step>=i+1?'#2563EB':'#D1D5DB'}">{{ s }}</span>
          </div>
          <div v-if="i<3" class="w-[100px] h-0.5 mb-5 rounded-[1px]" :style="{background:(step>i+1||step===i+1)?'#2563EB':'#D1D5DB'}"></div>
        </div>
      </div>
    </div>

    <!-- 表单卡片 -->
    <div class="flex-1 flex justify-center py-10 px-6">
      <div class="bg-white p-10" style="width:840px; border:1px solid rgba(0,0,0,0.25); border-radius:15px;">

        <!-- Step 1: 账号与联系人 -->
        <div v-if="step===1">
          <h3 class="text-[24px] font-bold text-gray-900 mb-1">企业注册</h3>
          <p class="text-[14px] mb-8" style="color:#757575;">请填写联系人账号信息，后续可用于登录企业门户。</p>
          <div class="grid grid-cols-2 gap-x-5 gap-y-4">
            <div><label class="block text-[14px] mb-1.5" style="color:#000;">联系人姓名 <span style="color:#EF4444;">*</span></label><input v-model="f.cn" placeholder="请输入联系人姓名" class="w-full px-4 py-2.5 text-[13px] focus:outline-none" style="border:1px solid rgba(0,0,0,0.16); border-radius:8px;" /></div>
            <div><label class="block text-[14px] mb-1.5" style="color:#000;">手机号 <span style="color:#EF4444;">*</span></label><input v-model="f.ph" placeholder="请输入手机号" class="w-full px-4 py-2.5 text-[13px] focus:outline-none" style="border:1px solid rgba(0,0,0,0.16); border-radius:8px;" /></div>
            <div><label class="block text-[14px] mb-1.5" style="color:#000;">身份证号 <span style="color:#EF4444;">*</span></label><input v-model="f.id" placeholder="请输入身份证号" class="w-full px-4 py-2.5 text-[13px] focus:outline-none" style="border:1px solid rgba(0,0,0,0.16); border-radius:8px;" /></div>
            <div><label class="block text-[14px] mb-1.5" style="color:#000;">邮箱 <span style="color:#EF4444;">*</span></label><input v-model="f.em" placeholder="请输入邮箱地址" class="w-full px-4 py-2.5 text-[13px] focus:outline-none" style="border:1px solid rgba(0,0,0,0.16); border-radius:8px;" /></div>
            <div><label class="block text-[14px] mb-1.5" style="color:#000;">登录密码 <span style="color:#EF4444;">*</span></label><input v-model="f.pw" type="password" placeholder="请输入登录密码" class="w-full px-4 py-2.5 text-[13px] focus:outline-none" style="border:1px solid rgba(0,0,0,0.16); border-radius:8px;" /></div>
            <div><label class="block text-[14px] mb-1.5" style="color:#000;">确认密码 <span style="color:#EF4444;">*</span></label><input v-model="f.cp" type="password" placeholder="请再次输入密码" class="w-full px-4 py-2.5 text-[13px] focus:outline-none" style="border:1px solid rgba(0,0,0,0.16); border-radius:8px;" /></div>
            <div><label class="block text-[14px] mb-1.5" style="color:#000;">验证码 <span style="color:#EF4444;">*</span></label>
              <div class="flex gap-2">
                <input v-model="f.sm" placeholder="请输入验证码" class="flex-1 px-4 py-2.5 text-[13px] focus:outline-none" style="border:1px solid rgba(0,0,0,0.16); border-radius:8px;" />
                <button @click="sendSms" :disabled="c>0" class="w-[120px] py-2.5 text-[13px] font-medium text-white rounded-[8px]" :style="{background:c>0?'#D1D5DB':'#2563EB'}">{{ c>0?c+'s':'发送验证码' }}</button>
              </div>
            </div>
          </div>
          <div class="flex items-center gap-2 mt-8 text-[13px]" style="color:#757575;">
            <div class="w-[18px] h-[18px] flex items-center justify-center cursor-pointer rounded-[3px]" :class="ok?'bg-[#2563EB] border-[#2563EB]':'border border-gray-300'" @click="ok=!ok">
              <span v-if="ok" class="text-[12px] font-bold text-white">✓</span>
            </div>
            <span>
              我已阅读并同意
              <router-link to="/user-agreement" class="font-medium hover:underline" style="color:#2563EB;">《用户服务协议》</router-link>
              和
              <router-link to="/privacy-policy" class="font-medium hover:underline" style="color:#2563EB;">《隐私政策》</router-link>
            </span>
          </div>
          <div class="flex justify-center mt-8 pt-6">
            <button @click="ok?step=2:showToast('请先阅读并同意服务协议和隐私政策')" class="w-[160px] h-[48px] text-[16px] font-bold text-white" style="background:#2563EB; border-radius:24px;">下一步</button>
          </div>
        </div>

        <!-- Step 2: 企业基本信息 -->
        <div v-if="step===2">
          <h3 class="text-[24px] font-bold text-gray-900 mb-1">企业基本信息</h3>
          <p class="text-[14px] mb-8" style="color:#757575;">请填写企业基础资料，便于学校进行资质审核。</p>
          <div class="grid grid-cols-2 gap-x-5 gap-y-4">
            <div><label class="block text-[14px] mb-1.5" style="color:#000;">企业全称 <span style="color:#EF4444;">*</span></label><input v-model="f.en" placeholder="请输入企业全称" class="w-full px-4 py-2.5 text-[13px] focus:outline-none" style="border:1px solid rgba(0,0,0,0.16); border-radius:8px;" /></div>
            <div><label class="block text-[14px] mb-1.5" style="color:#000;">统一社会信用代码 <span style="color:#EF4444;">*</span></label><input v-model="f.sc" placeholder="请输入18位统一社会信用代码" class="w-full px-4 py-2.5 text-[13px] focus:outline-none" style="border:1px solid rgba(0,0,0,0.16); border-radius:8px;" /></div>
            <div>
              <label class="block text-[14px] mb-1.5" style="color:#000;">所属行业 <span style="color:#EF4444;">*</span></label>
              <input
                v-model="f.ind"
                list="industry-options"
                placeholder="请选择或输入所属行业"
                class="w-full px-4 py-2.5 text-[13px] focus:outline-none"
                style="border:1px solid rgba(0,0,0,0.16); border-radius:8px; background:#fff;"
              />
              <datalist id="industry-options">
                <option v-for="industry in industryOptions" :key="industry" :value="industry" />
              </datalist>
            </div>
            <div><label class="block text-[14px] mb-1.5" style="color:#000;">企业规模 <span style="color:#EF4444;">*</span></label><select v-model="f.si" class="w-full px-4 py-2.5 text-[13px] focus:outline-none appearance-none" style="border:1px solid rgba(0,0,0,0.16); border-radius:8px; background:#fff;"><option value="">请选择企业规模</option><option>50人以下</option><option>50-200人</option><option>200-500人</option><option>500人以上</option></select></div>
            <div><label class="block text-[14px] mb-1.5" style="color:#000;">企业地址 <span style="color:#EF4444;">*</span></label><input v-model="f.ad" placeholder="请输入企业详细地址" class="w-full px-4 py-2.5 text-[13px] focus:outline-none" style="border:1px solid rgba(0,0,0,0.16); border-radius:8px;" /></div>
            <div><label class="block text-[14px] mb-1.5" style="color:#000;">企业官网</label><input v-model="f.web" placeholder="请输入企业官网地址（选填）" class="w-full px-4 py-2.5 text-[13px] focus:outline-none" style="border:1px solid rgba(0,0,0,0.16); border-radius:8px;" /></div>
            <div class="col-span-2"><label class="block text-[14px] mb-1.5" style="color:#000;">企业简介 <span style="color:#EF4444;">*</span></label><textarea v-model="f.ds" rows="3" placeholder="请输入企业简介（200字以内）" class="w-full px-4 py-2.5 text-[13px] resize-none focus:outline-none" style="border:1px solid rgba(0,0,0,0.16); border-radius:8px;"></textarea></div>
            <div><label class="block text-[14px] mb-1.5" style="color:#000;">法人姓名 <span style="color:#EF4444;">*</span></label><input v-model="f.ln" placeholder="请输入法人姓名" class="w-full px-4 py-2.5 text-[13px] focus:outline-none" style="border:1px solid rgba(0,0,0,0.16); border-radius:8px;" /></div>
            <div><label class="block text-[14px] mb-1.5" style="color:#000;">法人联系方式 <span style="color:#EF4444;">*</span></label><input v-model="f.lp" placeholder="请输入法人联系方式" class="w-full px-4 py-2.5 text-[13px] focus:outline-none" style="border:1px solid rgba(0,0,0,0.16); border-radius:8px;" /></div>
          </div>
          <div class="flex justify-center mt-8 pt-6 gap-4">
            <button @click="step=1" class="w-[120px] h-[44px] text-[15px]" style="border:1px solid rgba(0,0,0,0.25); border-radius:24px; color:#6B7280; background:#fff;">上一步</button>
            <button @click="step=3" class="w-[120px] h-[44px] text-[15px] font-bold text-white" style="background:#2563EB; border-radius:24px;">下一步</button>
          </div>
        </div>

        <!-- Step 3: 资质材料上传 -->
        <div v-if="step===3">
          <h3 class="text-[24px] font-bold text-gray-900 mb-1">资质材料上传</h3>
          <p class="text-[14px] mb-8" style="color:#757575;">请上传企业审核所需材料，营业执照为必填项。</p>
          <div class="space-y-4">
            <div class="flex items-center px-6 py-5" style="background:#FAFAFA; border:1px solid rgba(0,0,0,0.12); border-radius:8px;"><span class="text-[28px] mr-4">📄</span><div class="flex-1"><p class="text-[15px] font-bold text-gray-900">营业执照 <span class="text-[11px] font-bold" style="color:#EF4444;">必填</span></p><p class="text-[12px]" style="color:#757575;">支持 JPG / PNG / PDF，≤10MB</p></div><button @click="triggerUpload('biz')" class="w-[120px] h-[32px] text-[11px] cursor-pointer truncate px-2" style="border:1px solid #2563EB; border-radius:16px; color:#2563EB; background:#fff;">{{ uploadNames.biz || '上传文件' }}</button><input ref="uploadBiz" type="file" accept=".jpg,.jpeg,.png,.pdf" class="hidden" @change="onFileChange('biz', $event)" /></div>
            <div class="flex items-center px-6 py-5" style="background:#FAFAFA; border:1px solid rgba(0,0,0,0.12); border-radius:8px;"><span class="text-[28px] mr-4">📄</span><div class="flex-1"><p class="text-[15px] font-bold text-gray-900">高新技术企业资质 <span class="text-[11px] font-bold" style="color:#757575;">选填</span></p><p class="text-[12px]" style="color:#757575;">支持 JPG / PNG / PDF，≤10MB</p></div><button @click="triggerUpload('tech')" class="w-[120px] h-[32px] text-[11px] cursor-pointer truncate px-2" style="border:1px solid #2563EB; border-radius:16px; color:#2563EB; background:#fff;">{{ uploadNames.tech || '上传文件' }}</button><input ref="uploadTech" type="file" accept=".jpg,.jpeg,.png,.pdf" class="hidden" @change="onFileChange('tech', $event)" /></div>
            <div class="flex items-center px-6 py-5" style="background:#FAFAFA; border:1px solid rgba(0,0,0,0.12); border-radius:8px;"><span class="text-[28px] mr-4">📄</span><div class="flex-1"><p class="text-[15px] font-bold text-gray-900">财务报表 <span class="text-[11px] font-bold" style="color:#757575;">选填</span></p><p class="text-[12px]" style="color:#757575;">支持 JPG / PNG / PDF，≤10MB</p></div><button @click="triggerUpload('finance')" class="w-[120px] h-[32px] text-[11px] cursor-pointer truncate px-2" style="border:1px solid #2563EB; border-radius:16px; color:#2563EB; background:#fff;">{{ uploadNames.finance || '上传文件' }}</button><input ref="uploadFinance" type="file" accept=".jpg,.jpeg,.png,.pdf" class="hidden" @change="onFileChange('finance', $event)" /></div>
            <div class="flex items-center px-6 py-5" style="background:#FAFAFA; border:1px solid rgba(0,0,0,0.12); border-radius:8px;"><span class="text-[28px] mr-4">📄</span><div class="flex-1"><p class="text-[15px] font-bold text-gray-900">其他资质材料 <span class="text-[11px] font-bold" style="color:#757575;">选填</span></p><p class="text-[12px]" style="color:#757575;">可上传补充证明材料</p></div><button @click="triggerUpload('other')" class="w-[120px] h-[32px] text-[11px] cursor-pointer truncate px-2" style="border:1px solid #2563EB; border-radius:16px; color:#2563EB; background:#fff;">{{ uploadNames.other || '上传文件' }}</button><input ref="uploadOther" type="file" accept=".jpg,.jpeg,.png,.pdf" class="hidden" @change="onFileChange('other', $event)" /></div>
          </div>
          <div class="flex justify-center mt-8 pt-6 gap-4">
            <button @click="step=2" class="w-[120px] h-[44px] text-[15px]" style="border:1px solid rgba(0,0,0,0.25); border-radius:24px; color:#6B7280; background:#fff;">上一步</button>
            <button @click="step=4" class="w-[120px] h-[44px] text-[15px] font-bold text-white" style="background:#2563EB; border-radius:24px;">下一步</button>
          </div>
        </div>

        <!-- Step 4: 提交审核 -->
        <div v-if="step===4">
          <h3 class="text-[24px] font-bold text-gray-900 mb-1">提交审核</h3>
          <p class="text-[14px] mb-8" style="color:#757575;">请确认注册信息无误后提交审核。</p>
          <div class="rounded-xl p-6 mb-6" style="background:#F5F7FB;">
            <p class="text-[16px] font-bold text-gray-900 mb-3">对接主体选择</p>
            <div class="flex gap-6 mb-6">
              <div style="min-width:300px;">
                <div class="flex items-center gap-2 mb-1.5 cursor-pointer" @click="reviewTarget='college'">
                  <span class="w-[18px] h-[18px] flex items-center justify-center flex-shrink-0" :style="{border:'2px solid ' + (reviewTarget==='college'?'#2563EB':'#D1D5DB'), borderRadius:'50%'}"><span v-if="reviewTarget==='college'" class="w-[10px] h-[10px]" style="background:#2563EB; border-radius:50%;"></span></span>
                  <label class="text-[14px] cursor-pointer" style="color:#000;">对接学院</label>
                </div>
                <select v-if="reviewTarget==='college'" v-model="f.college" class="w-[300px] px-4 py-2 text-[13px] focus:outline-none" style="border:1px solid rgba(0,0,0,0.16); border-radius:8px; color:#374151; background:#fff;">
                  <option value="">请选择对接学院</option>
                  <option v-for="college in collegeOptions" :key="college" :value="college">{{ college }}</option>
                </select>
              </div>
              <div>
                <div class="flex items-center gap-2 mb-1.5 cursor-pointer" @click="reviewTarget='school'">
                  <span class="w-[18px] h-[18px] flex items-center justify-center flex-shrink-0" :style="{border:'2px solid ' + (reviewTarget==='school'?'#2563EB':'#D1D5DB'), borderRadius:'50%'}"><span v-if="reviewTarget==='school'" class="w-[10px] h-[10px]" style="background:#2563EB; border-radius:50%;"></span></span>
                  <label class="text-[14px] cursor-pointer" style="color:#000;">对接学校</label>
                </div>
                <p class="text-[12px] mt-1" style="color:#757575;">选择后将跳过学院初审，直接进入学校复核</p>
              </div>
            </div>
            <p class="text-[16px] font-bold text-gray-900 mb-3">信息确认摘要</p>
            <p class="text-[13px] font-bold mb-2" style="color:#2563EB;">联系人信息</p>
            <div class="grid grid-cols-2 gap-2 text-[13px] mb-3">
              <div><span style="color:#757575;">联系人姓名：</span><span style="color:#000; font-weight:700;">张三</span></div>
              <div><span style="color:#757575;">手机号：</span><span style="color:#000; font-weight:700;">138****8888</span></div>
              <div><span style="color:#757575;">身份证号：</span><span style="color:#000; font-weight:700;">**************1234</span></div>
            </div>
            <p class="text-[13px] font-bold mb-2" style="color:#2563EB;">企业信息</p>
            <div class="grid grid-cols-2 gap-2 text-[13px] mb-3">
              <div><span style="color:#757575;">企业名称：</span><span style="color:#000; font-weight:700;">无锡示例科技有限公司</span></div>
              <div><span style="color:#757575;">统一社会信用代码：</span><span style="color:#000; font-weight:700;">91320200XXXXXXXXXX</span></div>
              <div><span style="color:#757575;">所属行业：</span><span style="color:#000; font-weight:700;">信息技术服务</span></div>
              <div><span style="color:#757575;">企业规模：</span><span style="color:#000; font-weight:700;">51-200人</span></div>
            </div>
            <p class="text-[13px] font-bold mb-2" style="color:#2563EB;">资质材料</p>
            <div class="grid grid-cols-2 gap-2 text-[13px]">
              <div><span style="color:#757575;">营业执照：</span><span style="color:#000; font-weight:700;">✓ 已上传</span></div>
              <div><span style="color:#757575;">其他材料：</span><span style="color:#000; font-weight:700;">已上传 2 项</span></div>
            </div>
          </div>
          <p class="text-[12px] mb-4" style="color:#6B7280;">提交即代表您承诺以上信息真实有效，虚假信息将导致审核不通过。</p>
          <div class="flex justify-center pt-6 gap-4">
            <button @click="step=3" class="w-[120px] h-[44px] text-[15px]" style="border:1px solid rgba(0,0,0,0.25); border-radius:24px; color:#6B7280; background:#fff;">上一步</button>
            <button @click="done=true" class="w-[140px] h-[44px] text-[15px] font-bold text-white" style="background:#2563EB; border-radius:24px;">提交审核</button>
          </div>
          <!-- 成功弹窗 -->
          <div v-if="done" class="fixed inset-0 flex items-center justify-center z-50" style="background:rgba(0,0,0,0.3);">
            <div class="bg-white text-center" style="width:440px; border-radius:16px; padding:40px;">
              <div class="w-16 h-16 mx-auto mb-4 flex items-center justify-center" style="background:#10B981; border-radius:50%;"><span class="text-white text-[32px] font-bold">✓</span></div>
              <h3 class="text-[20px] font-bold text-gray-900 mb-2">提交成功！</h3>
              <p class="text-[14px] mb-6" style="color:#757575;">预计 3-5 个工作日内完成审核，请耐心等待。</p>
              <router-link to="/portal/console" class="inline-block w-[160px] h-[44px] leading-[44px] text-[15px] font-bold text-white" style="background:#2563EB; border-radius:24px;">返回企业门户</router-link>
            </div>
          </div>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick } from 'vue'
const step=ref(1), ok=ref(false), c=ref(0), done=ref(false), reviewTarget=ref('college')
const steps=['账号与联系人','企业基本信息','资质材料上传','提交审核']
const f=reactive({cn:'',ph:'',id:'',em:'',pw:'',cp:'',sm:'',en:'',sc:'',ind:'',si:'',ad:'',web:'',ds:'',ln:'',lp:'',college:''})
const industryOptions = ['互联网/IT', '金融', '制造', '教育', '信息技术服务', '人工智能', '集成电路', '智能制造', '交通运输', '环境工程', '传媒艺术']
const collegeOptions = [
  '物联网工程学院',
  '网络空间安全学院',
  '电子信息工程学院',
  '集成电路科学与工程学院',
  '自动化学院（智能制造工程学院）',
  '交通与车辆工程学院',
  '环境科学与工程学院',
  '大气与遥感学院',
  '数字经济与管理学院',
  '应急管理学院',
  '传媒与艺术学院',
  '国际教育学院',
  '马克思主义学院（法学院）',
  '新西伯利亚学院',
  '应用技术学院（继续教育学院）',
  '公告基础教学部（外国语学院）',
]

// Toast
const toast = ref({ show: false, text: '' })
let toastTimer = null
function showToast(text) {
  toast.value = { show: true, text }
  clearTimeout(toastTimer)
  toastTimer = setTimeout(() => { toast.value.show = false }, 2500)
}

function sendSms(){
  if(!f.ph) { showToast('请先输入手机号'); return }
  c.value = 60
  showToast('验证码发送成功')
  const t = setInterval(() => { c.value--; if(c.value<=0) clearInterval(t) }, 1000)
}

// File uploads
const uploadBiz = ref(null), uploadTech = ref(null), uploadFinance = ref(null), uploadOther = ref(null)
const uploadNames = reactive({ biz: '', tech: '', finance: '', other: '' })
const uploadRefs = { biz: uploadBiz, tech: uploadTech, finance: uploadFinance, other: uploadOther }
function triggerUpload(key) {
  uploadRefs[key].value?.click()
}
function onFileChange(key, e) {
  const file = e.target.files[0]
  if (file) uploadNames[key] = file.name
  e.target.value = ''
}
</script>

<style scoped>
.toast-fade-enter-active,
.toast-fade-leave-active {
  transition: opacity 0.3s ease;
}
.toast-fade-enter-from,
.toast-fade-leave-to {
  opacity: 0;
}
</style>
