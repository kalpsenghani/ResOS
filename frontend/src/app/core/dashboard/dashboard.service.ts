import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../shared/models/auth.model';
import { DashboardKpis, RecentOrder, RevenueChart } from '../../shared/models/dashboard.model';

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/dashboard`;

  getKpis(restaurantId: string, period = 'WEEK'): Observable<ApiResponse<DashboardKpis>> {
    const params = new HttpParams().set('restaurantId', restaurantId).set('period', period);
    return this.http.get<ApiResponse<DashboardKpis>>(`${this.baseUrl}/kpis`, { params });
  }

  getRecentOrders(restaurantId: string, limit = 10): Observable<ApiResponse<RecentOrder[]>> {
    const params = new HttpParams().set('restaurantId', restaurantId).set('limit', limit);
    return this.http.get<ApiResponse<RecentOrder[]>>(`${this.baseUrl}/recent-orders`, { params });
  }

  getRevenueChart(
    restaurantId: string,
    period = 'WEEK',
    groupBy = 'DAY',
  ): Observable<ApiResponse<RevenueChart>> {
    const params = new HttpParams()
      .set('restaurantId', restaurantId)
      .set('period', period)
      .set('groupBy', groupBy);
    return this.http.get<ApiResponse<RevenueChart>>(`${this.baseUrl}/revenue-chart`, { params });
  }
}
