import { RouterLink, RouterLinkActive } from '@angular/router';
import { TranslocoModule } from '@ngneat/transloco';
import { ThemeService } from '../../services/theme.service';
import { MobileMenuService } from '../../services/mobile-menu.service';
import { UserService } from '../../services/user.service';
import { ChangeDetectionStrategy, Component, computed, effect, HostListener, inject, signal } from '@angular/core';

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
  private readonly userService = inject(UserService);

  readonly avatarFailed = signal(false);
  readonly showAvatarImage = computed(() => !!this.userAvatarUrl() && !this.avatarFailed());

  businessName = 'Studio Armonia';
  businessSubtitle = 'Spațiu business · București';

  
  readonly currentUser = this.userService.currentUser;

  constructor() {
    effect(() => {
      this.userAvatarUrl();
      this.avatarFailed.set(false);
    });
  }

  readonly userName = computed(() => {
    const u = this.currentUser();
    return u ? `${u.firstName} ${u.lastName}` : '';
  });

  readonly userRole = computed(() => {
    switch (this.currentUser()?.role) {
      case 'ADMIN':
        return 'Administrator';
      case 'EMPLOYEE':
        return 'Employee';
      case 'CLIENT':
        return 'Client';
      default:
        return '';
    }
  });

  readonly userInitials = computed(() => {
    const u = this.currentUser();
    if (!u) return '';
    return `${u.firstName?.charAt(0) ?? ''}${u.lastName?.charAt(0) ?? ''}`.toUpperCase();
  });

  
  readonly userAvatarUrl = this.userService.avatarUrl;

  navItems: NavItem[] = [
    { path: '/discover', labelKey: 'NAV.DISCOVER', iconClass: 'fa-regular fa-compass' },
    {
      path: '/appointments',
      labelKey: 'NAV.APPOINTMENTS',
      iconClass: 'fa-regular fa-calendar',
      badge: 9,
    },
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
