import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Store } from '@ngrx/store';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from '../auth/auth.service';
import { AuthActions } from '../../store/auth/auth.actions';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const store = inject(Store);
  const authService = inject(AuthService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && !req.url.includes('/auth/')) {
        return authService.refresh().pipe(
          switchMap((response) => {
            store.dispatch(AuthActions.refreshSuccess({ response: response.data }));
            const retryReq = req.clone({
              setHeaders: { Authorization: `Bearer ${response.data.accessToken}` },
            });
            return next(retryReq);
          }),
          catchError(() => {
            store.dispatch(AuthActions.logout());
            return throwError(() => error);
          }),
        );
      }
      return throwError(() => error);
    }),
  );
};
