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
        loadComponent: () =>
          import('../placeholder/feature-placeholder.component').then((m) => m.FeaturePlaceholderComponent),
        data: {
          title: 'Menu',
          icon: 'restaurant_menu',
          subtitle: 'Menu builder',
          message: 'Build menus and categories — coming in Phase 8.',
        },
      },
      {
        path: 'orders',
        loadComponent: () =>
          import('../placeholder/feature-placeholder.component').then((m) => m.FeaturePlaceholderComponent),
        data: {
          title: 'Orders',
          icon: 'receipt_long',
          subtitle: 'Order management',
          message: 'Live order queue and history — coming in Phase 8.',
        },
      },
      {
        path: 'analytics',
        loadComponent: () =>
          import('../placeholder/feature-placeholder.component').then((m) => m.FeaturePlaceholderComponent),
        data: {
          title: 'Analytics',
          icon: 'analytics',
          subtitle: 'Business insights',
          message: 'Advanced analytics dashboards — coming in Phase 9.',
        },
      },
      {
        path: 'settings',
        loadChildren: () => import('../settings/settings.routes').then((m) => m.settingsRoutes),
      },
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
    ],
  },
];
