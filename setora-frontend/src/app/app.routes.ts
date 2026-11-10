import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'appointments' },
  { path: 'appointments', data: { breadcrumb: 'NAV.APPOINTMENTS' }, children: [] },
];