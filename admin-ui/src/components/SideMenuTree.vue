<script setup lang="ts">
import { useRouter } from 'vue-router';
import type { MenuNode } from '../types/system';
import { resolveIcon } from '../utils/icons';

defineOptions({ name: 'SideMenuTree' });

defineProps<{
  menus: MenuNode[];
}>();

const router = useRouter();

function navigate(path?: string) {
  if (path) {
    router.push(path);
  }
}
</script>

<template>
  <template v-for="menu in menus" :key="menu.path || menu.id">
    <el-sub-menu
      v-if="menu.children && menu.children.length > 0"
      :key="menu.path || String(menu.id)"
      :index="menu.path || String(menu.id)"
    >
      <template #title>
        <el-icon><component :is="resolveIcon(menu.icon)" /></el-icon>
        <span>{{ menu.name }}</span>
      </template>
      <SideMenuTree :menus="menu.children" />
    </el-sub-menu>

    <el-menu-item v-else :key="menu.path || String(menu.id)" :index="menu.path || String(menu.id)" @click="navigate(menu.path)">
      <el-icon><component :is="resolveIcon(menu.icon)" /></el-icon>
      <span>{{ menu.name }}</span>
    </el-menu-item>
  </template>
</template>
