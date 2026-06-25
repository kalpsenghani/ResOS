import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { Store } from '@ngrx/store';
import { map, take } from 'rxjs';
import { authFeature } from '../../store/auth/auth.reducer';

/** Send signed-in users to the app; everyone else sees the marketing page. */
export const landingGuard: CanActivateFn = () => {
  const store = inject(Store);
  const router = inject(Router);

  return store.select(authFeature.selectIsAuthenticated).pipe(
    take(1),
    map((isAuthenticated) => (isAuthenticated ? router.createUrlTree(['/dashboard']) : true)),
  );
};
