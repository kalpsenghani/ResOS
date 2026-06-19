import { createActionGroup, emptyProps, props } from '@ngrx/store';
import { TenantDetails, UpdateTenantRequest } from '../../core/tenant/tenant.service';

export const TenantActions = createActionGroup({
  source: 'Tenant',
  events: {
    'Load Current': emptyProps(),
    'Load Current Success': props<{ tenant: TenantDetails }>(),
    'Load Current Failure': props<{ error: string }>(),
    'Update Current': props<{ request: UpdateTenantRequest }>(),
    'Update Current Success': props<{ tenant: TenantDetails }>(),
    'Update Current Failure': props<{ error: string }>(),
    'Clear Error': emptyProps(),
  },
});
