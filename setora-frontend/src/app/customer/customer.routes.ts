import { Routes } from '@angular/router';

export const CUSTOMER_ROUTES: Routes = [
    { path: '', redirectTo: 'discover', pathMatch: 'full' },
    {
        path: 'discover',
        data: { breadcrumb: 'NAV.DISCOVER' },
        loadComponent: () =>
            import('./pages/discover/discover.component').then(m => m.DiscoverComponent)
    },
    { path: '**', redirectTo: 'discover' }
];