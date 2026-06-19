import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../shared/models/auth.model';

export interface TenantDetails {
  id: string;
  name: string;
  slug: string;
  email: string;
  phone?: string;
  timezone: string;
  currency: string;
  locale: string;
  status: string;
  settings: Record<string, unknown>;
  subscription?: {
    plan: string;
    status: string;
    currentPeriodEnd?: string;
    trialEndsAt?: string;
  };
}

export interface UpdateTenantRequest {
  name?: string;
  phone?: string;
  timezone?: string;
  currency?: string;
  settings?: Record<string, unknown>;
}

@Injectable({ providedIn: 'root' })
export class TenantService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/tenants`;

  getCurrent(): Observable<ApiResponse<TenantDetails>> {
    return this.http.get<ApiResponse<TenantDetails>>(`${this.baseUrl}/current`);
  }

  updateCurrent(request: UpdateTenantRequest): Observable<ApiResponse<TenantDetails>> {
    return this.http.put<ApiResponse<TenantDetails>>(`${this.baseUrl}/current`, request);
  }
}
