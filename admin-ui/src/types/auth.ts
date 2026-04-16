import type { MenuNode } from './system';

export interface LoginPayload {
  username: string;
  password: string;
}

export interface LoginResult {
  accessToken: string;
  tokenType: string;
  expiresAt: number;
}

export interface CurrentUserProfile {
  userId: number;
  username: string;
  displayName: string;
  roleIds: number[];
  permissions: string[];
  menus: MenuNode[];
}
