import { EnvironmentProviders } from '@angular/core';
import { provideEffects } from '@ngrx/effects';
import { provideState } from '@ngrx/store';
import { AuthEffects } from './auth/auth.effects';
import { authFeature } from './auth/auth.reducer';

export const storeProviders: EnvironmentProviders[] = [
  provideState(authFeature),
  provideEffects([AuthEffects]),
];
