import { createActionGroup, emptyProps, props } from '@ngrx/store';
import { AuthResponse, LoginRequest, RegisterRequest, UserProfile } from '../../shared/models/auth.model';

export const AuthActions = createActionGroup({
  source: 'Auth',
  events: {
    Login: props<{ request: LoginRequest }>(),
    Register: props<{ request: RegisterRequest }>(),
    'Auth Success': props<{ response: AuthResponse }>(),
    'Refresh Success': props<{ response: AuthResponse }>(),
    'Load Profile': emptyProps(),
    'Load Profile Success': props<{ profile: UserProfile }>(),
    'Auth Failure': props<{ error: string }>(),
    Logout: emptyProps(),
    'Clear Error': emptyProps(),
  },
});
