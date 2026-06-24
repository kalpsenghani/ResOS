import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { ApiResponse } from '../../../shared/models/auth.model';
import {
  CreateEmployeeRequest,
  CreateScheduleRequest,
  Employee,
  EmployeeSchedule,
  EmployeeStatus,
  UpdateEmployeeRequest,
} from '../../../shared/models/employee.model';

@Injectable({ providedIn: 'root' })
export class EmployeeService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/employees`;

  list(params: {
    restaurantId?: string;
    status?: EmployeeStatus;
    position?: string;
    search?: string;
    page?: number;
    size?: number;
  }): Observable<ApiResponse<Employee[]>> {
    let httpParams = new HttpParams();
    if (params.restaurantId) httpParams = httpParams.set('restaurantId', params.restaurantId);
    if (params.status) httpParams = httpParams.set('status', params.status);
    if (params.position) httpParams = httpParams.set('position', params.position);
    if (params.search) httpParams = httpParams.set('search', params.search);
    if (params.page != null) httpParams = httpParams.set('page', params.page);
    if (params.size != null) httpParams = httpParams.set('size', params.size);
    return this.http.get<ApiResponse<Employee[]>>(this.baseUrl, { params: httpParams });
  }

  create(request: CreateEmployeeRequest): Observable<ApiResponse<Employee>> {
    return this.http.post<ApiResponse<Employee>>(this.baseUrl, request);
  }

  update(id: string, request: UpdateEmployeeRequest, version?: number): Observable<ApiResponse<Employee>> {
    const headers = version != null ? { 'If-Match': String(version) } : undefined;
    return this.http.put<ApiResponse<Employee>>(`${this.baseUrl}/${id}`, request, { headers });
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  listSchedules(employeeId: string, startDate?: string, endDate?: string): Observable<ApiResponse<EmployeeSchedule[]>> {
    let params = new HttpParams();
    if (startDate) params = params.set('startDate', startDate);
    if (endDate) params = params.set('endDate', endDate);
    return this.http.get<ApiResponse<EmployeeSchedule[]>>(`${this.baseUrl}/${employeeId}/schedules`, { params });
  }

  createSchedule(employeeId: string, request: CreateScheduleRequest): Observable<ApiResponse<EmployeeSchedule>> {
    return this.http.post<ApiResponse<EmployeeSchedule>>(`${this.baseUrl}/${employeeId}/schedules`, request);
  }

  deleteSchedule(scheduleId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/schedules/${scheduleId}`);
  }
}
