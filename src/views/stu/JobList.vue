<!--
页面编号：STU-JOB-01
页面名称：企业需求浏览
路由：/stu/jobs
Figma Node：72:41
Figma Frame：164:2934
当前阶段：第二轮-按 Figma 验收
-->
<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import LayoutBackend from '../../layouts/LayoutBackend.vue'
import { jobListings } from '../../mock/stu.js'

const router = useRouter()
const searchQuery = ref('')
const typeFilter = ref('')
const cityFilter = ref('')
const educationFilter = ref('')
const sortFilter = ref('latest')

const filteredList = computed(() => {
  const keyword = searchQuery.value.trim().toLowerCase()
  const list = jobListings.filter((job) => {
    const matchesKeyword = !keyword || [job.company, job.position, job.location, ...job.tags]
      .some(value => value.toLowerCase().includes(keyword))
    const matchesType = !typeFilter.value || job.type === typeFilter.value
    const matchesCity = !cityFilter.value || job.location === cityFilter.value
    const matchesEducation = !educationFilter.value || job.education === educationFilter.value
    return matchesKeyword && matchesType && matchesCity && matchesEducation
  })

  return [...list].sort((a, b) => sortFilter.value === 'deadline'
    ? a.deadline.localeCompare(b.deadline)
    : b.publishDate.localeCompare(a.publishDate))
})

function apply(id) {
  router.push('/stu/internships/new')
}
</script>

<template>
  <LayoutBackend>
    <div class="px-[40px] pt-[18px] pb-[32px] min-h-full">
      <div class="mb-[16px]">
        <div class="text-[14px] font-semibold text-[#111827] mb-[4px]">通知公告 / 企业需求浏览</div>
        <h1 class="text-[32px] leading-[40px] font-bold text-black">企业需求浏览</h1>
        <p class="text-[16px] text-[#757575] mt-[2px]">搜索和浏览企业发布的实习岗位需求，选择心仪岗位报名</p>
      </div>

      <section class="bg-white rounded-[12px] px-[22px] py-[14px] mb-[18px] flex items-end gap-[20px]" aria-label="岗位筛选">
        <label class="relative flex-1 max-w-[280px]">
          <span class="absolute left-[14px] top-1/2 -translate-y-1/2 text-[#9ca3af] text-[13px]">⌕</span>
          <input
            v-model="searchQuery"
            type="search"
            placeholder="搜索企业或岗位名称..."
            class="w-full h-[36px] rounded-[8px] border border-[#c7c7c7] pl-[34px] pr-[12px] text-[13px] outline-none focus:border-[#2663eb]"
          />
        </label>

        <label class="w-[140px]">
          <span class="block text-[12px] text-[#6b7380] mb-[3px]">实习类型</span>
          <select v-model="typeFilter" class="filter-select">
            <option value="">全部类型</option>
            <option>毕业实习</option>
            <option>灵活实习</option>
          </select>
        </label>

        <label class="w-[140px]">
          <span class="block text-[12px] text-[#6b7380] mb-[3px]">所在城市</span>
          <select v-model="cityFilter" class="filter-select">
            <option value="">全部城市</option>
            <option>深圳</option>
            <option>北京</option>
            <option>上海</option>
            <option>杭州</option>
          </select>
        </label>

        <label class="w-[140px]">
          <span class="block text-[12px] text-[#6b7380] mb-[3px]">学历要求</span>
          <select v-model="educationFilter" class="filter-select">
            <option value="">不限</option>
            <option>本科</option>
            <option>本科及以上</option>
          </select>
        </label>

        <label class="w-[140px]">
          <span class="block text-[12px] text-[#6b7380] mb-[3px]">发布时间</span>
          <select v-model="sortFilter" class="filter-select">
            <option value="latest">最新发布</option>
            <option value="deadline">截止时间</option>
          </select>
        </label>
      </section>

      <p class="text-[14px] font-semibold text-black mb-[12px]">共找到 {{ filteredList.length }} 个岗位</p>

      <div v-if="filteredList.length === 0" class="bg-white rounded-[12px] py-[64px] text-center text-[#9ca3af] text-[14px]">
        暂无匹配的企业需求
      </div>

      <div v-else class="space-y-[16px]">
        <article
          v-for="job in filteredList"
          :key="job.id"
          class="min-h-[120px] bg-white rounded-[12px] px-[22px] py-[16px] flex items-start gap-[14px] hover:shadow-[0_8px_22px_rgba(15,23,42,0.08)] transition-shadow"
        >
          <div class="w-[48px] h-[48px] rounded-[8px] bg-[#f0f2f2] text-[#2663eb] text-[18px] font-bold flex items-center justify-center shrink-0 mt-[4px]">
            {{ job.logo }}
          </div>

          <div class="min-w-0 flex-1">
            <h2 class="text-[16px] leading-[20px] font-semibold text-black">{{ job.company }}</h2>
            <h3 class="text-[15px] leading-[20px] font-semibold text-[#1f1f1f]">{{ job.position }}</h3>
            <div class="flex flex-wrap items-center gap-[8px] mt-[6px]">
              <span class="meta-tag">{{ job.location }}</span>
              <span class="meta-tag">{{ job.type }}</span>
              <span class="meta-tag">{{ job.education }}</span>
              <span class="meta-tag">{{ job.vacancies }}</span>
              <span v-for="tag in job.tags" :key="tag" class="skill-tag">{{ tag }}</span>
            </div>
            <p class="text-[13px] leading-[18px] text-[#6b7380] mt-[7px] truncate max-w-[650px]">{{ job.description }}</p>
          </div>

          <div class="w-[220px] shrink-0 flex items-start justify-between gap-[14px] pt-[4px]">
            <span class="text-[12px] text-[#e5591a] whitespace-nowrap">⏰ 截止: {{ job.deadline }}</span>
            <button
              class="h-[32px] px-[18px] rounded-[8px] bg-[#2663eb] text-white text-[13px] font-semibold hover:bg-[#1f52c8] transition-colors cursor-pointer whitespace-nowrap"
              @click="apply(job.id)"
            >立即报名</button>
          </div>
        </article>
      </div>

      <nav class="h-[48px] mt-[8px] bg-white rounded-[12px] flex items-center justify-center gap-[28px] text-[14px] text-[#6b7380]" aria-label="分页">
        <button class="page-button" disabled>‹</button>
        <button class="w-[32px] h-[32px] rounded-[6px] bg-[#2663eb] text-white font-bold">1</button>
        <button class="page-button">2</button>
        <button class="page-button">3</button>
        <span>…</span>
        <button class="page-button">6</button>
        <button class="page-button">›</button>
      </nav>
    </div>
  </LayoutBackend>
</template>

<style scoped>
.filter-select {
  width: 100%;
  height: 36px;
  padding: 0 12px;
  border: 1px solid #c7c7c7;
  border-radius: 8px;
  background: #fff;
  color: #111827;
  font-size: 13px;
  outline: none;
}

.filter-select:focus {
  border-color: #2663eb;
}

.meta-tag,
.skill-tag {
  height: 22px;
  padding: 0 10px;
  border-radius: 6px;
  display: inline-flex;
  align-items: center;
  font-size: 11px;
  white-space: nowrap;
}

.meta-tag {
  background: #f5f7fa;
  color: #6b7380;
}

.skill-tag {
  background: #ebf2ff;
  color: #2663eb;
  font-weight: 600;
}

.page-button {
  min-width: 20px;
  height: 32px;
  cursor: pointer;
}

.page-button:disabled {
  opacity: 0.45;
  cursor: default;
}
</style>
