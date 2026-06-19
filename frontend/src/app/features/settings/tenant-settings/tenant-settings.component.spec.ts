import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideMockStore } from '@ngrx/store/testing';
import { tenantFeature } from '../../../store/tenant/tenant.reducer';
import { initialTenantState } from '../../../store/tenant/tenant.state';
import { TenantSettingsComponent } from './tenant-settings.component';

describe('TenantSettingsComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TenantSettingsComponent],
      providers: [
        provideRouter([]),
        provideMockStore({ initialState: { tenant: initialTenantState } }),
      ],
    }).compileComponents();
  });

  it('should create', () => {
    const fixture = TestBed.createComponent(TenantSettingsComponent);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('should expose tenant selectors', () => {
    const fixture = TestBed.createComponent(TenantSettingsComponent);
    expect(fixture.componentInstance.loading()).toBe(false);
  });
});
