import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { ApiResponse } from '../../../shared/models/auth.model';
import {
  CreateOrderRequest,
  Order,
  OrderItem,
  OrderItemStatus,
  OrderStatus,
} from '../../../shared/models/order.model';

@Injectable({ providedIn: 'root' })
export class OrderService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/orders`;

  list(params: {
    restaurantId?: string;
    status?: OrderStatus;
    date?: string;
    page?: number;
    size?: number;
  }): Observable<ApiResponse<Order[]>> {
    let httpParams = new HttpParams();
    if (params.restaurantId) httpParams = httpParams.set('restaurantId', params.restaurantId);
    if (params.status) httpParams = httpParams.set('status', params.status);
    if (params.date) httpParams = httpParams.set('date', params.date);
    if (params.page != null) httpParams = httpParams.set('page', params.page);
    if (params.size != null) httpParams = httpParams.set('size', params.size);
    return this.http.get<ApiResponse<Order[]>>(this.baseUrl, { params: httpParams });
  }

  create(request: CreateOrderRequest): Observable<ApiResponse<Order>> {
    return this.http.post<ApiResponse<Order>>(this.baseUrl, request);
  }

  get(id: string): Observable<ApiResponse<Order>> {
    return this.http.get<ApiResponse<Order>>(`${this.baseUrl}/${id}`);
  }

  updateStatus(id: string, status: OrderStatus, notes?: string): Observable<ApiResponse<Order>> {
    return this.http.patch<ApiResponse<Order>>(`${this.baseUrl}/${id}/status`, { status, notes });
  }

  updateItemStatus(orderId: string, itemId: string, status: OrderItemStatus): Observable<ApiResponse<OrderItem>> {
    return this.http.patch<ApiResponse<OrderItem>>(`${this.baseUrl}/${orderId}/items/${itemId}/status`, { status });
  }
}
