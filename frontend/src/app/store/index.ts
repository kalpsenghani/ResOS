import { EnvironmentProviders } from '@angular/core';
import { provideEffects } from '@ngrx/effects';
import { provideState } from '@ngrx/store';
import { AuthEffects } from './auth/auth.effects';
import { authFeature } from './auth/auth.reducer';
import { TenantEffects } from './tenant/tenant.effects';
import { tenantFeature } from './tenant/tenant.reducer';

export const storeProviders: EnvironmentProviders[] = [
  provideState(authFeature),
  provideState(tenantFeature),
  provideEffects([AuthEffects, TenantEffects]),
];
