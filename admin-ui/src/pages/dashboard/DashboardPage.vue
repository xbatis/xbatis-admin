<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { Avatar, Checked, DataBoard, Grid } from '@element-plus/icons-vue';
import { getDashboardOverview } from '../../api/dashboard';
import { useAuthStore } from '../../stores/auth';
import type { DashboardOverview } from '../../types/system';

const authStore = useAuthStore();
const loading = ref(false);
const overview = ref<DashboardOverview>({
  userCount: 0,
  roleCount: 0,
  menuCount: 0,
  enabledUserCount: 0
});

const cards = computed(() => [
  {
    title: '系统用户',
    value: overview.value.userCount,
    note: '当前数据库中可管理的全部账号数量',
    tone: 'blue',
    icon: Avatar
  },
  {
    title: '启用用户',
    value: overview.value.enabledUserCount,
    note: '仍然处于启用状态并可登录的用户数',
    tone: 'green',
    icon: Checked
  },
  {
    title: '角色总数',
    value: overview.value.roleCount,
    note: '已经定义好的权限包和授权角色集合',
    tone: 'orange',
    icon: DataBoard
  },
  {
    title: '菜单节点',
    value: overview.value.menuCount,
    note: '目录、页面和按钮权限节点的总数',
    tone: 'slate',
    icon: Grid
  }
]);

async function loadData() {
  loading.value = true;
  try {
    overview.value = await getDashboardOverview();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '加载看板失败');
  } finally {
    loading.value = false;
  }
}

onMounted(loadData);
</script>

<template>
  <div class="page-stack">
    <section class="hero-panel hero-panel--dashboard">
      <div class="hero-copy">
        <div class="hero-kicker">System Snapshot</div>
        <h2>欢迎回来，{{ authStore.profile?.displayName }}</h2>
        <p>
          这里是当前权限系统的控制台总览。用户、角色、菜单和按钮权限由同一套结构驱动，前端页面只展示当前会话真正可见的边界。
        </p>

        <div class="hero-tags">
          <span>Spring Boot 3</span>
          <span>xbatis</span>
          <span>PostgreSQL</span>
          <span>Vue 3</span>
          <span>Element Plus</span>
        </div>
      </div>
    </section>

    <section class="stat-grid">
      <article v-for="card in cards" :key="card.title" class="stat-card" :data-tone="card.tone">
        <div class="stat-card__head">
          <div class="stat-card__icon" :data-tone="card.tone">
            <el-icon><component :is="card.icon" /></el-icon>
          </div>
          <span>{{ card.title }}</span>
        </div>
        <strong>{{ card.value }}</strong>
        <p>{{ card.note }}</p>
      </article>
    </section>

    <section class="surface-panel">
      <div class="section-head">
        <div>
          <p class="section-kicker">Architecture Notes</p>
          <h3>权限模型摘要</h3>
        </div>
      </div>

      <div class="dashboard-matrix">
        <div>
          <strong>用户层</strong>
          <p>负责承载登录身份、启停状态、基础信息和最后登录时间，是整个权限链路的起点。</p>
        </div>
        <div>
          <strong>角色层</strong>
          <p>将权限打包成可分配的角色集合，同一用户可挂多个角色，通过角色组合决定操作边界。</p>
        </div>
        <div>
          <strong>菜单层</strong>
          <p>目录、页面路由和按钮权限统一建模，前端只渲染当前会话真正可访问的入口与动作。</p>
        </div>
      </div>
    </section>
  </div>
</template>
