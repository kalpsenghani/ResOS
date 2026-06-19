import { inject, Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { catchError, map, of, switchMap } from 'rxjs';
import { TenantService } from '../../core/tenant/tenant.service';
import { TenantActions } from './tenant.actions';

@Injectable()
export class TenantEffects {
  private readonly actions$ = inject(Actions);
  private readonly tenantService = inject(TenantService);

  loadCurrent$ = createEffect(() =>
    this.actions$.pipe(
      ofType(TenantActions.loadCurrent),
      switchMap(() =>
        this.tenantService.getCurrent().pipe(
          map((response) => TenantActions.loadCurrentSuccess({ tenant: response.data })),
          catchError((error) =>
            of(TenantActions.loadCurrentFailure({ error: error?.error?.error?.message ?? 'Failed to load tenant' })),
          ),
        ),
      ),
    ),
  );

  updateCurrent$ = createEffect(() =>
    this.actions$.pipe(
      ofType(TenantActions.updateCurrent),
      switchMap(({ request }) =>
        this.tenantService.updateCurrent(request).pipe(
          map((response) => TenantActions.updateCurrentSuccess({ tenant: response.data })),
          catchError((error) =>
            of(TenantActions.updateCurrentFailure({ error: error?.error?.error?.message ?? 'Failed to update tenant' })),
          ),
        ),
      ),
    ),
  );
}
