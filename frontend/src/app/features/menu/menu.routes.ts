import { Routes } from '@angular/router';

export const menuRoutes: Routes = [
  {
    path: '',
    loadComponent: () => import('./menu-list/menu-list.component').then((m) => m.MenuListComponent),
  },
];
