import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Store } from '@ngrx/store';
import { map, take } from 'rxjs';
import { authFeature } from '../../store/auth/auth.reducer';

export const authGuard: CanActivateFn = () => {
  const store = inject(Store);
  const router = inject(Router);

  return store.select(authFeature.selectIsAuthenticated).pipe(
    take(1),
    map((isAuthenticated) => isAuthenticated || router.createUrlTree(['/auth/login'])),
  );
};

export const guestGuard: CanActivateFn = () => {
  const store = inject(Store);
  const router = inject(Router);

  return store.select(authFeature.selectIsAuthenticated).pipe(
    take(1),
    map((isAuthenticated) => !isAuthenticated || router.createUrlTree(['/dashboard'])),
  );
};
