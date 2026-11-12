import { ChangeDetectionStrategy, Component, HostListener, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { TranslocoModule } from '@ngneat/transloco';
import { ThemeService } from '../../services/theme.service';
import { MobileMenuService } from '../../services/mobile-menu.service';

interface NavItem {
  path: string;
  labelKey: string;
  iconClass: string;
  badge?: number;
}

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, TranslocoModule],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SidebarComponent {
  readonly menu = inject(MobileMenuService);
  readonly theme = inject(ThemeService);

  businessName = 'Studio Armonia';
  businessSubtitle = 'Spațiu business · București';
  userName = 'Emma Pavel';
  userRole = 'Administrator';
  userInitials = 'EP';

  navItems: NavItem[] = [
    { path: '/appointments', labelKey: 'NAV.APPOINTMENTS', iconClass: 'fa-regular fa-calendar', badge: 9 },
    { path: '/services', labelKey: 'NAV.SERVICES', iconClass: 'fa-solid fa-briefcase' },
    { path: '/employees', labelKey: 'NAV.EMPLOYEES', iconClass: 'fa-solid fa-users' },
    { path: '/clients', labelKey: 'NAV.CLIENTS', iconClass: 'fa-regular fa-id-card' },
    { path: '/locations', labelKey: 'NAV.LOCATIONS', iconClass: 'fa-solid fa-location-dot' },
    { path: '/working-hours', labelKey: 'NAV.WORKING_HOURS', iconClass: 'fa-regular fa-clock' },
  ];

  @HostListener('document:keydown.escape')
  onEsc(): void {
    this.menu.close();
  }
}