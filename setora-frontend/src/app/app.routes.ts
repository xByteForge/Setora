import { Routes } from '@angular/router';
import { authGuard, guestGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'discover', pathMatch: 'full' },

  
  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./pages/auth/login/login.component').then((m) => m.LoginComponent),
  },
  {
    path: 'register',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./pages/auth/register/register.component').then((m) => m.RegisterComponent),
  },

  
  {
    path: '',
    canActivate: [authGuard],
    children: [
      {
        path: 'discover',
        data: { breadcrumb: 'NAV.DISCOVER' },
        loadComponent: () =>
          import('./pages/discover/discover.component').then((m) => m.DiscoverComponent),
      },
      {
        path: 'appointments',
        data: { breadcrumb: 'NAV.APPOINTMENTS' },
        loadComponent: () =>
          import('./pages/appointments/appointments.component').then((m) => m.AppointmentsComponent),
      },
      {
        path: 'services',
        data: { breadcrumb: 'NAV.SERVICES' },
        loadComponent: () =>
          import('./pages/services/services.component').then((m) => m.ServicesComponent),
      },
      {
        path: 'employees',
        data: { breadcrumb: 'NAV.EMPLOYEES' },
        loadComponent: () =>
          import('./pages/employees/employees.component').then((m) => m.EmployeesComponent),
      },
      {
        path: 'settings',
        data: { breadcrumb: 'NAV.SETTINGS' },
        loadComponent: () =>
          import('./pages/settings/settings.component').then((m) => m.SettingsComponent),
      },
      {
        path: 'locations',
        data: { breadcrumb: 'NAV.LOCATIONS' },
        loadComponent: () =>
          import('./pages/locations/locations.component').then((m) => m.LocationsComponent),
      },
      {
        path: 'working-hours',
        data: { breadcrumb: 'NAV.WORKING_HOURS' },
        loadComponent: () =>
          import('./pages/working-hours/working-hours.component').then((m) => m.WorkingHoursComponent),
      },
      {
        path: 'clients',
        data: { breadcrumb: 'NAV.CLIENTS' },
        loadComponent: () =>
          import('./pages/clients/clients.component').then((m) => m.ClientsComponent),
      },
    ],
  },

  { path: '**', redirectTo: 'discover' },
];