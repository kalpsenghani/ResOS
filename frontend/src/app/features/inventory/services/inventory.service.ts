import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { ApiResponse } from '../../../shared/models/auth.model';
import {
  CreateInventoryItemRequest,
  CreateTransactionRequest,
  InventoryItem,
  InventoryTransaction,
  StockAlert,
  UpdateInventoryItemRequest,
} from '../../../shared/models/inventory.model';

@Injectable({ providedIn: 'root' })
export class InventoryService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/inventory`;

  list(params: {
    restaurantId?: string;
    category?: string;
    lowStock?: boolean;
    search?: string;
    page?: number;
    size?: number;
  }): Observable<ApiResponse<InventoryItem[]>> {
    let httpParams = new HttpParams();
    if (params.restaurantId) httpParams = httpParams.set('restaurantId', params.restaurantId);
    if (params.category) httpParams = httpParams.set('category', params.category);
    if (params.lowStock != null) httpParams = httpParams.set('lowStock', params.lowStock);
    if (params.search) httpParams = httpParams.set('search', params.search);
    if (params.page != null) httpParams = httpParams.set('page', params.page);
    if (params.size != null) httpParams = httpParams.set('size', params.size);
    return this.http.get<ApiResponse<InventoryItem[]>>(this.baseUrl, { params: httpParams });
  }

  get(id: string): Observable<ApiResponse<InventoryItem>> {
    return this.http.get<ApiResponse<InventoryItem>>(`${this.baseUrl}/${id}`);
  }

  create(request: CreateInventoryItemRequest): Observable<ApiResponse<InventoryItem>> {
    return this.http.post<ApiResponse<InventoryItem>>(this.baseUrl, request);
  }

  update(id: string, request: UpdateInventoryItemRequest, version?: number): Observable<ApiResponse<InventoryItem>> {
    const headers = version != null ? { 'If-Match': String(version) } : undefined;
    return this.http.put<ApiResponse<InventoryItem>>(`${this.baseUrl}/${id}`, request, { headers });
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  recordTransaction(itemId: string, request: CreateTransactionRequest): Observable<ApiResponse<InventoryTransaction>> {
    return this.http.post<ApiResponse<InventoryTransaction>>(`${this.baseUrl}/${itemId}/transactions`, request);
  }

  listAlerts(restaurantId?: string, acknowledged = false): Observable<ApiResponse<StockAlert[]>> {
    let params = new HttpParams().set('acknowledged', acknowledged);
    if (restaurantId) params = params.set('restaurantId', restaurantId);
    return this.http.get<ApiResponse<StockAlert[]>>(`${this.baseUrl}/alerts`, { params });
  }

  acknowledgeAlert(alertId: string): Observable<ApiResponse<StockAlert>> {
    return this.http.patch<ApiResponse<StockAlert>>(`${this.baseUrl}/alerts/${alertId}/acknowledge`, {});
  }
}
