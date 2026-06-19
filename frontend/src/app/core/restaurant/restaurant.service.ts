import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../shared/models/auth.model';
import { Restaurant } from '../../shared/models/restaurant.model';

@Injectable({ providedIn: 'root' })
export class RestaurantService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/restaurants`;

  list(): Observable<ApiResponse<Restaurant[]>> {
    return this.http.get<ApiResponse<Restaurant[]>>(this.baseUrl);
  }
}
