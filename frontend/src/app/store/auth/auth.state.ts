import { AuthResponse, TenantSummary, UserProfile, UserSummary } from '../../shared/models/auth.model';

export interface AuthState {
  accessToken: string | null;
  user: UserSummary | null;
  tenant: TenantSummary | null;
  profile: UserProfile | null;
  isAuthenticated: boolean;
  loading: boolean;
  error: string | null;
}

export const initialAuthState: AuthState = {
  accessToken: null,
  user: null,
  tenant: null,
  profile: null,
  isAuthenticated: false,
  loading: false,
  error: null,
};

export function authSuccessReducer(state: AuthState, response: AuthResponse): AuthState {
  return {
    ...state,
    accessToken: response.accessToken,
    user: response.user,
    tenant: response.tenant,
    isAuthenticated: true,
    loading: false,
    error: null,
  };
}
