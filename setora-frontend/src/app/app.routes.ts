import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: 'appointments', pathMatch: 'full' },
  { 
    path: 'appointments', 
    loadComponent: () => import('./pages/appointments/appointments.component').then(m => m.AppointmentsComponent) 
  },
  { 
    path: 'services', 
    loadComponent: () => import('./pages/services/services.component').then(m => m.ServicesComponent) 
  },
  { 
    path: 'employees', 
    loadComponent: () => import('./pages/employees/employees.component').then(m => m.EmployeesComponent) 
  },
  { 
    path: 'settings', 
    loadComponent: () => import('./pages/settings/settings.component').then(m => m.SettingsComponent) 
  },
  {
    path: 'locations',
    loadComponent: () => import('./pages/locations/locations.component').then(m => m.LocationsComponent)
  },
  {
    path: 'working-hours',
    loadComponent: () => import('./pages/working-hours/working-hours.component').then(m => m.WorkingHoursComponent)
  },
  {
    path: 'clients',
    loadComponent: () => import('./pages/clients/clients.component').then(m => m.ClientsComponent)
  },
  { path: '**', redirectTo: 'appointments' }
];