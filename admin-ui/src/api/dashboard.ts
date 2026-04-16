import { request } from './http';
import type { DashboardOverview } from '../types/system';

export function getDashboardOverview() {
  return request<DashboardOverview>({
    url: '/api/dashboard/overview',
    method: 'get'
  });
}
