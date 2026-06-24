import { Routes } from '@angular/router';

export const reservationsRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./reservation-list/reservation-list.component').then((m) => m.ReservationListComponent),
  },
];
