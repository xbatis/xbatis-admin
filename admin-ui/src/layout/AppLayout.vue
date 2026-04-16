<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { Expand, Fold, RefreshRight, SwitchButton } from '@element-plus/icons-vue';
import { useRoute, useRouter } from 'vue-router';
import SideMenuTree from '../components/SideMenuTree.vue';
import { useAuthStore } from '../stores/auth';
import { findAncestorKeys } from '../utils/tree';

const authStore = useAuthStore();
const route = useRoute();
const router = useRouter();

const collapsed = ref(false);
const openKeys = ref<string[]>([]);

const menuTree = computed(() => (authStore.profile?.menus ?? []).filter((item) => item.visible === 1));
const pageTitle = computed(() => String(route.meta.title || '后台管理'));
const pageRoute = computed(() => route.path);
const roleSummary = computed(() => authStore.profile?.roleNames?.join(' · ') || 'No active roles');
const menuRenderKey = computed(() => `${collapsed.value ? 'collapsed' : 'expanded'}:${route.path}:${openKeys.value.join('|')}`);

watch(
  () => [route.path, menuTree.value],
  () => {
    openKeys.value = findAncestorKeys(menuTree.value, route.path);
  },
  { immediate: true, deep: true }
);

async function refreshProfile() {
  try {
    authStore.initialized = false;
    await authStore.bootstrap();
    ElMessage.success('权限信息已刷新');
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '刷新权限失败');
  }
}

function logout() {
  authStore.logout();
  router.push('/login');
}
</script>

<template>
  <div class="app-shell">
    <aside class="app-sider" :class="{ 'is-collapsed': collapsed }">
      <div class="app-sider__glow"></div>

      <div class="brand-block">
        <div class="brand-mark">SX</div>
        <div v-if="!collapsed" class="brand-copy">
          <strong>Xbatis-Admin</strong>
          <span class="brand-copy__eyebrow">Access Matrix</span>
        </div>
      </div>

      <el-scrollbar class="nav-scroll">
        <el-menu
          :key="menuRenderKey"
          :default-active="route.path"
          :default-openeds="collapsed ? [] : openKeys"
          :collapse="collapsed"
          :collapse-transition="false"
          class="nav-menu"
        >
          <SideMenuTree :menus="menuTree" />
        </el-menu>
      </el-scrollbar>
    </aside>

    <div class="app-main">
      <header class="app-header">
        <div class="header-left">
          <el-button text class="header-icon" @click="collapsed = !collapsed">
            <el-icon><Expand v-if="collapsed" /><Fold v-else /></el-icon>
          </el-button>

          <div class="page-meta">
            <div class="page-kicker">RBAC CONTROL ROOM</div>
            <div class="page-headline">
              <div class="page-title">{{ pageTitle }}</div>
              <div class="page-route">{{ pageRoute }}</div>
            </div>
          </div>
        </div>

        <div class="header-actions">
          <el-button text class="header-action" @click="refreshProfile">
            <el-icon><RefreshRight /></el-icon>
            刷新权限
          </el-button>

          <div class="user-badge">
            <div class="user-name">{{ authStore.profile?.displayName || '未登录' }}</div>
            <div class="user-role">{{ roleSummary }}</div>
          </div>

          <el-button text class="header-action header-action-danger" @click="logout">
            <el-icon><SwitchButton /></el-icon>
            退出
          </el-button>
        </div>
      </header>

      <main class="app-content">
        <div class="content-stage">
          <router-view />
        </div>
      </main>
    </div>
  </div>
</template>
