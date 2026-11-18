import { ChangeDetectionStrategy, Component, HostListener, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink, RouterLinkActive } from '@angular/router';
import { TranslocoModule } from '@ngneat/transloco';
import { filter, map, startWith } from 'rxjs';
import { ThemeService } from '../../services/theme.service';
import { MobileMenuService } from '../../services/mobile-menu.service';
import { BusinessFiltersComponent } from './business-filters/business-filters.component';

interface NavItem {
    path: string;
    labelKey: string;
    iconClass: string;
    disabled?: boolean; // pagina nu există încă
}

@Component({
    selector: 'app-customer-sidebar',
    standalone: true,
    imports: [RouterLink, RouterLinkActive, TranslocoModule, BusinessFiltersComponent],
    templateUrl: './customer-sidebar.component.html',
    styleUrl: './customer-sidebar.component.css',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CustomerSidebarComponent {
    readonly menu = inject(MobileMenuService);
    readonly theme = inject(ThemeService);
    private readonly router = inject(Router);

    userName = 'Ana Popescu';
    userRole = 'Client';
    userInitials = 'AP';

    navItems: NavItem[] = [
        { path: '/customer/discover', labelKey: 'NAV.DISCOVER', iconClass: 'fa-regular fa-compass' },
        { path: '/customer/bookings', labelKey: 'NAV.BOOKINGS', iconClass: 'fa-regular fa-calendar-check', disabled: true },
    ];

    private readonly url = toSignal(
        this.router.events.pipe(
            filter((e): e is NavigationEnd => e instanceof NavigationEnd),
            map((e) => e.urlAfterRedirects),
            startWith(this.router.url),
        ),
        { initialValue: this.router.url },
    );

    // filtrele apar doar pe pagina de discover
    protected showFilters(): boolean {
        return this.url().startsWith('/customer/discover');
    }

    @HostListener('document:keydown.escape')
    onEsc(): void {
        this.menu.close();
    }
}