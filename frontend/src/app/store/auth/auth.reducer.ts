import { createFeature, createReducer, createSelector, on } from '@ngrx/store';
import { AuthActions } from './auth.actions';
import { authSuccessReducer, initialAuthState } from './auth.state';

export const authFeature = createFeature({
  name: 'auth',
  reducer: createReducer(
    initialAuthState,
    on(AuthActions.login, AuthActions.register, (state) => ({
      ...state,
      loading: true,
      error: null,
    })),
    on(AuthActions.authSuccess, AuthActions.refreshSuccess, (state, { response }) =>
      authSuccessReducer(state, response),
    ),
    on(AuthActions.loadProfileSuccess, (state, { profile }) => ({
      ...state,
      profile,
    })),
    on(AuthActions.authFailure, (state, { error }) => ({
      ...state,
      loading: false,
      error,
    })),
    on(AuthActions.logout, () => initialAuthState),
    on(AuthActions.clearError, (state) => ({ ...state, error: null })),
  ),
});

export const {
  selectAuthState,
  selectAccessToken,
  selectUser,
  selectTenant,
  selectProfile,
  selectIsAuthenticated,
  selectLoading,
  selectError,
} = authFeature;

export const selectRoles = createSelector(authFeature.selectUser, (user) => user?.roles ?? []);

export const selectPermissions = createSelector(
  authFeature.selectUser,
  (user) => user?.permissions ?? [],
);
