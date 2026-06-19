import { Component, inject, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Store } from '@ngrx/store';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatButtonModule } from '@angular/material/button';
import { SidebarComponent, SidebarNavItem } from '../../components/sidebar/sidebar.component';
import { ButtonComponent } from '../../components/button/button.component';
import { ThemeService } from '../../../core/theme/theme.service';
import { AuthActions } from '../../../store/auth/auth.actions';
import { authFeature } from '../../../store/auth/auth.reducer';
import { tenantFeature } from '../../../store/tenant/tenant.reducer';

const NAV_ITEMS: SidebarNavItem[] = [
  { label: 'Dashboard', icon: 'dashboard', route: '/dashboard' },
  { label: 'Orders', icon: 'receipt_long', route: '/orders' },
  { label: 'Menu', icon: 'restaurant_menu', route: '/menu' },
  { label: 'Inventory', icon: 'inventory_2', route: '/inventory' },
  { label: 'Employees', icon: 'groups', route: '/employees' },
  { label: 'Reservations', icon: 'event_seat', route: '/reservations' },
  { label: 'Analytics', icon: 'analytics', route: '/analytics' },
  { label: 'Settings', icon: 'settings', route: '/settings' },
];

@Component({
  selector: 'app-dashboard-layout',
  imports: [RouterOutlet, SidebarComponent, ButtonComponent, MatIconModule, MatMenuModule, MatButtonModule],
  templateUrl: './dashboard-layout.component.html',
  styleUrl: './dashboard-layout.component.scss',
})
export class DashboardLayoutComponent {
  private readonly store = inject(Store);
  protected readonly theme = inject(ThemeService);

  readonly navItems = NAV_ITEMS;
  readonly sidebarCollapsed = signal(false);
  readonly user = this.store.selectSignal(authFeature.selectUser);
  readonly tenant = this.store.selectSignal(authFeature.selectTenant);
  readonly tenantDetails = this.store.selectSignal(tenantFeature.selectCurrent);

  toggleSidebar(): void {
    this.sidebarCollapsed.update((value) => !value);
  }

  logout(): void {
    this.store.dispatch(AuthActions.logout());
  }
}
