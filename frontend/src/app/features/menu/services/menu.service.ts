import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { ApiResponse } from '../../../shared/models/auth.model';
import {
  CreateCategoryRequest,
  CreateMenuItemRequest,
  MenuCategory,
  MenuItem,
  UpdateCategoryRequest,
  UpdateMenuItemRequest,
} from '../../../shared/models/menu.model';

@Injectable({ providedIn: 'root' })
export class MenuService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/menu`;

  listCategories(restaurantId: string): Observable<ApiResponse<MenuCategory[]>> {
    return this.http.get<ApiResponse<MenuCategory[]>>(`${this.baseUrl}/categories`, {
      params: { restaurantId },
    });
  }

  createCategory(request: CreateCategoryRequest): Observable<ApiResponse<MenuCategory>> {
    return this.http.post<ApiResponse<MenuCategory>>(`${this.baseUrl}/categories`, request);
  }

  updateCategory(id: string, request: UpdateCategoryRequest): Observable<ApiResponse<MenuCategory>> {
    return this.http.put<ApiResponse<MenuCategory>>(`${this.baseUrl}/categories/${id}`, request);
  }

  deleteCategory(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/categories/${id}`);
  }

  listItems(params: {
    restaurantId?: string;
    categoryId?: string;
    available?: boolean;
    search?: string;
  }): Observable<ApiResponse<MenuItem[]>> {
    let httpParams = new HttpParams();
    if (params.restaurantId) httpParams = httpParams.set('restaurantId', params.restaurantId);
    if (params.categoryId) httpParams = httpParams.set('categoryId', params.categoryId);
    if (params.available != null) httpParams = httpParams.set('available', params.available);
    if (params.search) httpParams = httpParams.set('search', params.search);
    return this.http.get<ApiResponse<MenuItem[]>>(`${this.baseUrl}/items`, { params: httpParams });
  }

  createItem(request: CreateMenuItemRequest): Observable<ApiResponse<MenuItem>> {
    return this.http.post<ApiResponse<MenuItem>>(`${this.baseUrl}/items`, request);
  }

  updateItem(id: string, request: UpdateMenuItemRequest, version?: number): Observable<ApiResponse<MenuItem>> {
    const headers = version != null ? { 'If-Match': String(version) } : undefined;
    return this.http.put<ApiResponse<MenuItem>>(`${this.baseUrl}/items/${id}`, request, { headers });
  }

  setAvailability(id: string, isAvailable: boolean): Observable<ApiResponse<MenuItem>> {
    return this.http.patch<ApiResponse<MenuItem>>(`${this.baseUrl}/items/${id}/availability`, { isAvailable });
  }

  deleteItem(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/items/${id}`);
  }
}
