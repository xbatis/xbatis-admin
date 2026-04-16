import { defineStore } from 'pinia';
import { getProfile, login } from '../api/auth';
import { clearToken, getToken, persistToken } from '../api/http';
import type { CurrentUserProfile, LoginPayload } from '../types/auth';

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: getToken(),
    profile: null as CurrentUserProfile | null,
    initialized: false
  }),
  actions: {
    async bootstrap() {
      if (!this.token) {
        this.initialized = true;
        return;
      }
      try {
        this.profile = await getProfile();
      } catch {
        this.logout();
      } finally {
        this.initialized = true;
      }
    },
    async signIn(payload: LoginPayload) {
      const result = await login(payload);
      this.token = result.accessToken;
      persistToken(result.accessToken);
      this.initialized = false;
      await this.bootstrap();
    },
    logout() {
      this.token = '';
      this.profile = null;
      this.initialized = true;
      clearToken();
    },
    hasPermission(permission: string) {
      return this.profile?.permissions?.includes(permission) ?? false;
    }
  }
});
