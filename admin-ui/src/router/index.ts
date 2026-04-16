import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '../stores/auth';
import { pinia } from '../stores/pinia';

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      component: () => import('../pages/auth/LoginPage.vue'),
      meta: { public: true, title: '登录' }
    },
    {
      path: '/',
      component: () => import('../layout/AppLayout.vue'),
      children: [
        { path: '', redirect: '/dashboard' },
        {
          path: '/dashboard',
          component: () => import('../pages/dashboard/DashboardPage.vue'),
          meta: { title: '工作台' }
        },
        {
          path: '/system/users',
          component: () => import('../pages/system/UsersPage.vue'),
          meta: { title: '用户管理' }
        },
        {
          path: '/system/roles',
          component: () => import('../pages/system/RolesPage.vue'),
          meta: { title: '角色管理' }
        },
        {
          path: '/system/menus',
          component: () => import('../pages/system/MenusPage.vue'),
          meta: { title: '菜单管理' }
        }
      ]
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/dashboard'
    }
  ]
});

router.beforeEach(async (to) => {
  const authStore = useAuthStore(pinia);
  if (!authStore.initialized) {
    await authStore.bootstrap();
  }
  if (to.meta.public) {
    if (to.path === '/login' && authStore.token) {
      return '/dashboard';
    }
    return true;
  }
  if (!authStore.token) {
    return '/login';
  }
  return true;
});

router.afterEach((to) => {
  const title = typeof to.meta.title === 'string' ? to.meta.title : 'Xbatis-Admin Admin';
  document.title = `${title} · Xbatis-Admin Admin`;
});

export default router;
