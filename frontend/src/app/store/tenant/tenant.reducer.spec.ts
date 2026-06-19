import { tenantFeature } from './tenant.reducer';
import { initialTenantState } from './tenant.state';
import { TenantActions } from './tenant.actions';

describe('Tenant Reducer', () => {
  it('should return initial state', () => {
    const state = tenantFeature.reducer(undefined, { type: 'unknown' });
    expect(state).toEqual(initialTenantState);
  });

  it('should set loading on loadCurrent', () => {
    const state = tenantFeature.reducer(initialTenantState, TenantActions.loadCurrent());
    expect(state.loading).toBe(true);
  });
});
