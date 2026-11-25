import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: 'discover', pathMatch: 'full' },
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
  {
    path: 'register-business',
    data: {
      breadcrumb: 'BUSINESS_REGISTRATION.TITLE',
      hideSidebar: true,
      topbarBackUrl: '',
      topbarBackLabelKey: 'BUSINESS_REGISTRATION.BACK_TO_AUTH',
    },
    loadComponent: () =>
      import('./pages/business-registration/business-registration.component').then(
        (m) => m.BusinessRegistrationComponent,
      ),
  },
  {
    path: 'my-businesses',
    data: { breadcrumb: 'NAV.MY_BUSINESSES' },
    loadComponent: () =>
        import('./pages/my-businesses/my-businesses.component').then(
            (m) => m.MyBusinessesComponent,
        ),
  },

  { path: '**', redirectTo: 'discover' },
];
