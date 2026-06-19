import { createFeature, createReducer, on } from '@ngrx/store';
import { TenantActions } from './tenant.actions';
import { initialTenantState } from './tenant.state';

export const tenantFeature = createFeature({
  name: 'tenant',
  reducer: createReducer(
    initialTenantState,
    on(TenantActions.loadCurrent, (state) => ({ ...state, loading: true, error: null })),
    on(TenantActions.loadCurrentSuccess, (state, { tenant }) => ({
      ...state,
      current: tenant,
      loading: false,
    })),
    on(TenantActions.loadCurrentFailure, (state, { error }) => ({
      ...state,
      loading: false,
      error,
    })),
    on(TenantActions.updateCurrent, (state) => ({ ...state, saving: true, error: null })),
    on(TenantActions.updateCurrentSuccess, (state, { tenant }) => ({
      ...state,
      current: tenant,
      saving: false,
    })),
    on(TenantActions.updateCurrentFailure, (state, { error }) => ({
      ...state,
      saving: false,
      error,
    })),
    on(TenantActions.clearError, (state) => ({ ...state, error: null })),
  ),
});

export const {
  selectCurrent,
  selectLoading,
  selectSaving,
  selectError,
} = tenantFeature;
