import { Component, inject } from '@angular/core';
import { Store } from '@ngrx/store';
import { MatButtonModule } from '@angular/material/button';
import { AuthActions } from '../../store/auth/auth.actions';
import { authFeature } from '../../store/auth/auth.reducer';

@Component({
  selector: 'app-dashboard',
  imports: [MatButtonModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
})
export class DashboardComponent {
  private readonly store = inject(Store);

  readonly user = this.store.selectSignal(authFeature.selectUser);
  readonly tenant = this.store.selectSignal(authFeature.selectTenant);

  logout(): void {
    this.store.dispatch(AuthActions.logout());
  }
}
