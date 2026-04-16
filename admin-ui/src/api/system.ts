import { request } from './http';
import type { PageResponse } from '../types/common';
import type {
  MenuFormPayload,
  MenuNode,
  MenuPageItem,
  OptionItem,
  RoleFormPayload,
  RoleItem,
  UserFormPayload,
  UserItem
} from '../types/system';

export function getUsers(params: Record<string, unknown>) {
  return request<PageResponse<UserItem>>({
    url: '/api/system/users',
    method: 'get',
    params
  });
}

export function getUserDetail(id: number) {
  return request<UserItem>({
    url: `/api/system/users/${id}`,
    method: 'get'
  });
}

export function createUser(data: UserFormPayload) {
  return request<number>({
    url: '/api/system/users',
    method: 'post',
    data
  });
}

export function updateUser(id: number, data: UserFormPayload) {
  return request<void>({
    url: `/api/system/users/${id}`,
    method: 'put',
    data
  });
}

export function updateUserStatus(id: number, enabled: number) {
  return request<void>({
    url: `/api/system/users/${id}/status`,
    method: 'put',
    data: { enabled }
  });
}

export function resetUserPassword(id: number) {
  return request<void>({
    url: `/api/system/users/${id}/reset-password`,
    method: 'put'
  });
}

export function getRoles(params: Record<string, unknown>) {
  return request<PageResponse<RoleItem>>({
    url: '/api/system/roles',
    method: 'get',
    params
  });
}

export function getRoleOptions() {
  return request<OptionItem[]>({
    url: '/api/system/roles/options',
    method: 'get'
  });
}

export function getRoleDetail(id: number) {
  return request<RoleItem>({
    url: `/api/system/roles/${id}`,
    method: 'get'
  });
}

export function createRole(data: RoleFormPayload) {
  return request<number>({
    url: '/api/system/roles',
    method: 'post',
    data
  });
}

export function updateRole(id: number, data: RoleFormPayload) {
  return request<void>({
    url: `/api/system/roles/${id}`,
    method: 'put',
    data
  });
}

export function deleteRole(id: number) {
  return request<void>({
    url: `/api/system/roles/${id}`,
    method: 'delete'
  });
}

export function getMenus() {
  return request<MenuNode[]>({
    url: '/api/system/menus',
    method: 'get'
  });
}

export function getMenuDetail(id: number) {
  return request<MenuNode>({
    url: `/api/system/menus/${id}`,
    method: 'get'
  });
}

export function getMenuPage(params: Record<string, unknown>) {
  return request<PageResponse<MenuPageItem>>({
    url: '/api/system/menus/page',
    method: 'get',
    params
  });
}

export function getMenuOptions() {
  return request<MenuNode[]>({
    url: '/api/system/menus/options',
    method: 'get'
  });
}

export function createMenu(data: MenuFormPayload) {
  return request<number>({
    url: '/api/system/menus',
    method: 'post',
    data
  });
}

export function updateMenu(id: number, data: MenuFormPayload) {
  return request<void>({
    url: `/api/system/menus/${id}`,
    method: 'put',
    data
  });
}

export function deleteMenu(id: number) {
  return request<void>({
    url: `/api/system/menus/${id}`,
    method: 'delete'
  });
}
