import { Routes } from '@angular/router';
import { authGuard } from '../../core/guards/auth.guard';

export const settingsRoutes: Routes = [
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./tenant-settings/tenant-settings.component').then((m) => m.TenantSettingsComponent),
  },
];
