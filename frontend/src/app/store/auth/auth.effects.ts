import { inject, Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { catchError, map, of, switchMap, tap } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';
import { AuthActions } from './auth.actions';
import { ApiError } from '../../shared/models/auth.model';

@Injectable()
export class AuthEffects {
  private readonly actions$ = inject(Actions);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  login$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AuthActions.login),
      switchMap(({ request }) =>
        this.authService.login(request).pipe(
          map((response) => AuthActions.authSuccess({ response: response.data })),
          catchError((error) => of(AuthActions.authFailure({ error: extractError(error) }))),
        ),
      ),
    ),
  );

  register$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AuthActions.register),
      switchMap(({ request }) =>
        this.authService.register(request).pipe(
          map((response) => AuthActions.authSuccess({ response: response.data })),
          catchError((error) => of(AuthActions.authFailure({ error: extractError(error) }))),
        ),
      ),
    ),
  );

  authSuccess$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType(AuthActions.authSuccess),
        tap(() => this.router.navigate(['/dashboard'])),
      ),
    { dispatch: false },
  );

  logout$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AuthActions.logout),
      switchMap(() =>
        this.authService.logout().pipe(
          map(() => AuthActions.clearError()),
          catchError(() => of(AuthActions.clearError())),
        ),
      ),
    ),
  );

  logoutRedirect$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType(AuthActions.logout),
        tap(() => this.router.navigate(['/login'])),
      ),
    { dispatch: false },
  );
}

function extractError(error: { error?: ApiError; message?: string }): string {
  const apiError = error.error?.error;
  if (apiError?.details?.length) {
    return apiError.details.join('; ');
  }
  return apiError?.message ?? error.message ?? 'An unexpected error occurred';
}
