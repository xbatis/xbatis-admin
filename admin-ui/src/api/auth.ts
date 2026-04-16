import { request } from './http';
import type { CurrentUserProfile, LoginPayload, LoginResult } from '../types/auth';

export function login(payload: LoginPayload) {
  return request<LoginResult>({
    url: '/api/auth/login',
    method: 'post',
    data: payload
  });
}

export function getProfile() {
  return request<CurrentUserProfile>({
    url: '/api/auth/profile',
    method: 'get'
  });
}
