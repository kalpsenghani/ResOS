import { TenantDetails } from '../../core/tenant/tenant.service';

export interface TenantState {
  current: TenantDetails | null;
  loading: boolean;
  saving: boolean;
  error: string | null;
}

export const initialTenantState: TenantState = {
  current: null,
  loading: false,
  saving: false,
  error: null,
};
