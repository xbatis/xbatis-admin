export interface MenuNode {
  id: number;
  parentId: number;
  name: string;
  type: 'CATALOG' | 'MENU' | 'BUTTON';
  path?: string;
  component?: string;
  icon?: string;
  permission?: string;
  redirect?: string;
  sort: number;
  visible: number;
  keepAlive: number;
  status: number;
  remark?: string;
  children?: MenuNode[];
}

export interface MenuPageItem extends MenuNode {
  depth: number;
  parentPathLabel: string;
}

export interface UserItem {
  id: number;
  username: string;
  displayName: string;
  email?: string;
  phone?: string;
  enabled: number;
  remark?: string;
  lastLoginAt?: string;
  createdAt?: string;
  roleIds: number[];
  roleNames: string[];
}

export interface UserFormPayload {
  username: string;
  displayName: string;
  email?: string;
  phone?: string;
  enabled: number;
  remark?: string;
  roleIds: number[];
}

export interface RoleItem {
  id: number;
  name: string;
  enabled: number;
  sort: number;
  deletable: number;
  remark?: string;
  createdAt?: string;
  menuIds?: number[];
  userCount?: number;
}

export interface RoleFormPayload {
  name: string;
  enabled: number;
  sort: number;
  deletable: number;
  remark?: string;
  menuIds: number[];
}

export interface MenuFormPayload {
  parentId: number;
  name: string;
  type: 'CATALOG' | 'MENU' | 'BUTTON';
  path?: string;
  component?: string;
  icon?: string;
  permission?: string;
  redirect?: string;
  sort: number;
  visible: number;
  keepAlive: number;
  status: number;
  remark?: string;
}

export interface OptionItem {
  value: number;
  label: string;
}

export interface DashboardOverview {
  userCount: number;
  roleCount: number;
  menuCount: number;
  enabledUserCount: number;
}
