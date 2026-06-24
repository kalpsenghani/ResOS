import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { ApiResponse } from '../../../shared/models/auth.model';
import {
  AvailabilityResponse,
  CreateReservationRequest,
  CreateTableRequest,
  Reservation,
  ReservationStatus,
  RestaurantTable,
  UpdateReservationRequest,
  UpdateTableRequest,
} from '../../../shared/models/reservation.model';

@Injectable({ providedIn: 'root' })
export class ReservationService {
  private readonly http = inject(HttpClient);
  private readonly reservationsUrl = `${environment.apiUrl}/reservations`;
  private readonly tablesUrl = `${environment.apiUrl}/tables`;

  listReservations(params: {
    restaurantId?: string;
    date?: string;
    startDate?: string;
    endDate?: string;
    status?: ReservationStatus;
    search?: string;
    page?: number;
    size?: number;
  }): Observable<ApiResponse<Reservation[]>> {
    let httpParams = new HttpParams();
    if (params.restaurantId) httpParams = httpParams.set('restaurantId', params.restaurantId);
    if (params.date) httpParams = httpParams.set('date', params.date);
    if (params.startDate) httpParams = httpParams.set('startDate', params.startDate);
    if (params.endDate) httpParams = httpParams.set('endDate', params.endDate);
    if (params.status) httpParams = httpParams.set('status', params.status);
    if (params.search) httpParams = httpParams.set('search', params.search);
    if (params.page != null) httpParams = httpParams.set('page', params.page);
    if (params.size != null) httpParams = httpParams.set('size', params.size);
    return this.http.get<ApiResponse<Reservation[]>>(this.reservationsUrl, { params: httpParams });
  }

  createReservation(request: CreateReservationRequest): Observable<ApiResponse<Reservation>> {
    return this.http.post<ApiResponse<Reservation>>(this.reservationsUrl, request);
  }

  updateReservation(
    id: string,
    request: UpdateReservationRequest,
    version?: number,
  ): Observable<ApiResponse<Reservation>> {
    const headers = version != null ? { 'If-Match': String(version) } : undefined;
    return this.http.put<ApiResponse<Reservation>>(`${this.reservationsUrl}/${id}`, request, { headers });
  }

  updateStatus(id: string, status: ReservationStatus): Observable<ApiResponse<Reservation>> {
    return this.http.patch<ApiResponse<Reservation>>(`${this.reservationsUrl}/${id}/status`, { status });
  }

  cancel(id: string): Observable<void> {
    return this.http.delete<void>(`${this.reservationsUrl}/${id}`);
  }

  checkAvailability(params: {
    restaurantId: string;
    date: string;
    partySize: number;
    startTime: string;
  }): Observable<ApiResponse<AvailabilityResponse>> {
    let httpParams = new HttpParams()
      .set('restaurantId', params.restaurantId)
      .set('date', params.date)
      .set('partySize', params.partySize)
      .set('startTime', params.startTime);
    return this.http.get<ApiResponse<AvailabilityResponse>>(`${this.reservationsUrl}/availability`, {
      params: httpParams,
    });
  }

  listTables(restaurantId: string): Observable<ApiResponse<RestaurantTable[]>> {
    return this.http.get<ApiResponse<RestaurantTable[]>>(this.tablesUrl, {
      params: { restaurantId },
    });
  }

  createTable(request: CreateTableRequest): Observable<ApiResponse<RestaurantTable>> {
    return this.http.post<ApiResponse<RestaurantTable>>(this.tablesUrl, request);
  }

  updateTable(id: string, request: UpdateTableRequest): Observable<ApiResponse<RestaurantTable>> {
    return this.http.put<ApiResponse<RestaurantTable>>(`${this.tablesUrl}/${id}`, request);
  }

  deleteTable(id: string): Observable<void> {
    return this.http.delete<void>(`${this.tablesUrl}/${id}`);
  }
}
