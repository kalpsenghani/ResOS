import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Store } from '@ngrx/store';
import { authFeature } from '../../store/auth/auth.reducer';

export const tenantInterceptor: HttpInterceptorFn = (req, next) => {
  const store = inject(Store);
  const tenant = store.selectSignal(authFeature.selectTenant)();

  if (tenant?.id && !req.headers.has('X-Tenant-ID') && !isPublicAuthRequest(req.url)) {
    req = req.clone({
      setHeaders: { 'X-Tenant-ID': tenant.id },
    });
  }

  return next(req);
};

function isPublicAuthRequest(url: string): boolean {
  return url.includes('/auth/login') || url.includes('/auth/register') || url.includes('/auth/refresh');
}
