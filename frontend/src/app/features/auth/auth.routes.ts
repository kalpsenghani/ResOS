import { Routes } from '@angular/router';
import { AuthLayoutComponent } from '../../shared/layout/auth-layout/auth-layout.component';
import { guestGuard } from '../../core/guards/auth.guard';

export const publicAuthRoutes: Routes = [
  {
    path: 'login',
    component: AuthLayoutComponent,
    canActivate: [guestGuard],
    children: [
      { path: '', loadComponent: () => import('./login/login.component').then((m) => m.LoginComponent) },
    ],
  },
  {
    path: 'register',
    component: AuthLayoutComponent,
    canActivate: [guestGuard],
    children: [
      { path: '', loadComponent: () => import('./register/register.component').then((m) => m.RegisterComponent) },
    ],
  },
];

/** Backward-compatible redirects for old /auth/* URLs. */
export const authRoutes: Routes = [
  { path: 'login', redirectTo: '/login', pathMatch: 'full' },
  { path: 'register', redirectTo: '/register', pathMatch: 'full' },
  { path: '', pathMatch: 'full', redirectTo: '/login' },
];
