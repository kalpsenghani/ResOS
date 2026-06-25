import { Routes } from '@angular/router';
import { landingGuard } from './core/guards/landing.guard';
import { appShellRoutes } from './features/shell/shell.routes';
import { authRoutes, publicAuthRoutes } from './features/auth/auth.routes';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    canActivate: [landingGuard],
    loadComponent: () => import('./features/landing/landing.component').then((m) => m.LandingComponent),
  },
  ...publicAuthRoutes,
  {
    path: 'auth',
    children: authRoutes,
  },
  ...appShellRoutes,
  { path: '**', redirectTo: '' },
];
