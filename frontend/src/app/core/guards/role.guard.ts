import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Store } from '@ngrx/store';
import { map, take } from 'rxjs';
import { authFeature, selectRoles } from '../../store/auth/auth.reducer';

export const roleGuard = (roles: string[]): CanActivateFn => {
  return () => {
    const store = inject(Store);
    const router = inject(Router);

    return store.select(selectRoles).pipe(
      take(1),
      map((userRoles) => {
        const hasRole = roles.some((role) => userRoles.includes(role));
        return hasRole || router.createUrlTree(['/dashboard']);
      }),
    );
  };
};
