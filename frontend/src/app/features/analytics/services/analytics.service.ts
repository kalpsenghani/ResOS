import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { ApiResponse } from '../../../shared/models/auth.model';
import {
  EmployeeAnalytics,
  InventoryAnalytics,
  OrderAnalytics,
  RevenueAnalytics,
} from '../../../shared/models/analytics.model';

@Injectable({ providedIn: 'root' })
export class AnalyticsService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/analytics`;

  getRevenue(params: {
    restaurantId: string;
    startDate?: string;
    endDate?: string;
    groupBy?: string;
  }): Observable<ApiResponse<RevenueAnalytics>> {
    let httpParams = new HttpParams().set('restaurantId', params.restaurantId);
    if (params.startDate) httpParams = httpParams.set('startDate', params.startDate);
    if (params.endDate) httpParams = httpParams.set('endDate', params.endDate);
    if (params.groupBy) httpParams = httpParams.set('groupBy', params.groupBy);
    return this.http.get<ApiResponse<RevenueAnalytics>>(`${this.baseUrl}/revenue`, { params: httpParams });
  }

  getInventory(restaurantId: string, period = 'MONTH'): Observable<ApiResponse<InventoryAnalytics>> {
    return this.http.get<ApiResponse<InventoryAnalytics>>(`${this.baseUrl}/inventory`, {
      params: { restaurantId, period },
    });
  }

  getEmployees(params: {
    restaurantId: string;
    startDate?: string;
    endDate?: string;
  }): Observable<ApiResponse<EmployeeAnalytics>> {
    let httpParams = new HttpParams().set('restaurantId', params.restaurantId);
    if (params.startDate) httpParams = httpParams.set('startDate', params.startDate);
    if (params.endDate) httpParams = httpParams.set('endDate', params.endDate);
    return this.http.get<ApiResponse<EmployeeAnalytics>>(`${this.baseUrl}/employees`, { params: httpParams });
  }

  getOrders(params: {
    restaurantId: string;
    startDate?: string;
    endDate?: string;
  }): Observable<ApiResponse<OrderAnalytics>> {
    let httpParams = new HttpParams().set('restaurantId', params.restaurantId);
    if (params.startDate) httpParams = httpParams.set('startDate', params.startDate);
    if (params.endDate) httpParams = httpParams.set('endDate', params.endDate);
    return this.http.get<ApiResponse<OrderAnalytics>>(`${this.baseUrl}/orders`, { params: httpParams });
  }
}
