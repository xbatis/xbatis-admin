export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

export interface PageResponse<T> {
  records: T[];
  current: number;
  pageSize: number;
  total: number;
  totalPages: number;
}
