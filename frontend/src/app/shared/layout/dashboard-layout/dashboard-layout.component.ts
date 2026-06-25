import { Component, computed, inject, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Store } from '@ngrx/store';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { SidebarComponent, SidebarNavGroup } from '../../components/sidebar/sidebar.component';
import { ThemeService } from '../../../core/theme/theme.service';
import { AuthActions } from '../../../store/auth/auth.actions';
import { authFeature } from '../../../store/auth/auth.reducer';
import { tenantFeature } from '../../../store/tenant/tenant.reducer';

const NAV_GROUPS: SidebarNavGroup[] = [
  {
    label: 'Operations',
    items: [
      { label: 'Dashboard', icon: 'space_dashboard', route: '/dashboard' },
      { label: 'Orders', icon: 'receipt_long', route: '/orders' },
      { label: 'Reservations', icon: 'event_seat', route: '/reservations' },
    ],
  },
  {
    label: 'Catalog',
    items: [
      { label: 'Menu', icon: 'restaurant_menu', route: '/menu' },
      { label: 'Inventory', icon: 'inventory_2', route: '/inventory' },
    ],
  },
  {
    label: 'Management',
    items: [
      { label: 'Employees', icon: 'groups', route: '/employees' },
      { label: 'Analytics', icon: 'analytics', route: '/analytics' },
    ],
  },
  {
    label: 'System',
    items: [{ label: 'Settings', icon: 'settings', route: '/settings' }],
  },
];

@Component({
  selector: 'app-dashboard-layout',
  imports: [RouterOutlet, SidebarComponent, MatIconModule, MatMenuModule],
  templateUrl: './dashboard-layout.component.html',
  styleUrl: './dashboard-layout.component.scss',
})
export class DashboardLayoutComponent {
  private readonly store = inject(Store);
  protected readonly theme = inject(ThemeService);

  readonly navGroups = NAV_GROUPS;
  readonly sidebarCollapsed = signal(false);
  readonly user = this.store.selectSignal(authFeature.selectUser);
  readonly tenant = this.store.selectSignal(authFeature.selectTenant);
  readonly tenantDetails = this.store.selectSignal(tenantFeature.selectCurrent);

  readonly restaurantName = computed(
    () => this.tenantDetails()?.name ?? this.tenant()?.name ?? 'Restaurant',
  );

  readonly initials = computed(() => {
    const u = this.user();
    const first = u?.firstName?.charAt(0) ?? '';
    const last = u?.lastName?.charAt(0) ?? '';
    return (first + last).toUpperCase() || 'U';
  });

  toggleSidebar(): void {
    this.sidebarCollapsed.update((value) => !value);
  }

  logout(): void {
    this.store.dispatch(AuthActions.logout());
  }
}
