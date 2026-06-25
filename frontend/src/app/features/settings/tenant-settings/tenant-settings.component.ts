import { Component, effect, inject, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Store } from '@ngrx/store';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MotionInViewDirective } from '../../../shared/animations';
import { TenantActions } from '../../../store/tenant/tenant.actions';
import { tenantFeature } from '../../../store/tenant/tenant.reducer';

@Component({
  selector: 'app-tenant-settings',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatProgressSpinnerModule,
    MotionInViewDirective,
  ],
  templateUrl: './tenant-settings.component.html',
  styleUrl: './tenant-settings.component.scss',
})
export class TenantSettingsComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly store = inject(Store);

  readonly tenant = this.store.selectSignal(tenantFeature.selectCurrent);
  readonly loading = this.store.selectSignal(tenantFeature.selectLoading);
  readonly saving = this.store.selectSignal(tenantFeature.selectSaving);
  readonly error = this.store.selectSignal(tenantFeature.selectError);

  readonly form = this.fb.nonNullable.group({
    name: [''],
    phone: [''],
    timezone: [''],
    currency: [''],
  });

  constructor() {
    effect(() => {
      const current = this.tenant();
      if (current) {
        this.form.patchValue({
          name: current.name,
          phone: current.phone ?? '',
          timezone: current.timezone,
          currency: current.currency,
        });
      }
    });
  }

  ngOnInit(): void {
    this.store.dispatch(TenantActions.loadCurrent());
  }

  submit(): void {
    if (this.form.invalid) {
      return;
    }
    this.store.dispatch(TenantActions.updateCurrent({ request: this.form.getRawValue() }));
  }
}
