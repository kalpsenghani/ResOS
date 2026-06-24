import { Routes } from '@angular/router';
import { authGuard } from '../../core/guards/auth.guard';

export const appShellRoutes: Routes = [
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () =>
      import('../../shared/layout/dashboard-layout/dashboard-layout.component').then(
        (m) => m.DashboardLayoutComponent,
      ),
    children: [
      {
        path: 'dashboard',
        loadComponent: () =>
          import('../dashboard/dashboard.component').then((m) => m.DashboardComponent),
      },
      {
        path: 'inventory',
        loadChildren: () => import('../inventory/inventory.routes').then((m) => m.inventoryRoutes),
      },
      {
        path: 'employees',
        loadChildren: () => import('../employees/employees.routes').then((m) => m.employeesRoutes),
      },
      {
        path: 'reservations',
        loadChildren: () => import('../reservations/reservations.routes').then((m) => m.reservationsRoutes),
      },
      {
        path: 'menu',
        loadChildren: () => import('../menu/menu.routes').then((m) => m.menuRoutes),
      },
      {
        path: 'orders',
        loadChildren: () => import('../orders/orders.routes').then((m) => m.ordersRoutes),
      },
      {
        path: 'analytics',
        loadChildren: () => import('../analytics/analytics.routes').then((m) => m.analyticsRoutes),
      },
      {
        path: 'settings',
        loadChildren: () => import('../settings/settings.routes').then((m) => m.settingsRoutes),
      },
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
    ],
  },
];
