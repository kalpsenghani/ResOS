import { TestBed } from '@angular/core/testing';
import { authFeature, selectRoles } from './auth.reducer';
import { initialAuthState } from './auth.state';

describe('Auth Reducer', () => {
  it('should return initial state', () => {
    const state = authFeature.reducer(undefined, { type: 'unknown' });
    expect(state).toEqual(initialAuthState);
  });

  it('selectRoles should return user roles', () => {
    const roles = selectRoles.projector({
      id: '1',
      email: 'a@b.com',
      firstName: 'A',
      lastName: 'B',
      roles: ['TENANT_OWNER'],
      permissions: [],
    });
    expect(roles).toEqual(['TENANT_OWNER']);
  });
});
