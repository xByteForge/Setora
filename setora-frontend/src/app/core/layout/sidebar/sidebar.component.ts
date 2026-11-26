import { NavigationEnd, Router, RouterLink, RouterLinkActive } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { filter, map, startWith } from 'rxjs';
import { TranslocoModule } from '@ngneat/transloco';
import { ThemeService } from '../../services/theme.service';
import { MobileMenuService } from '../../services/mobile-menu.service';
import { UserService } from '../../services/user.service';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  effect,
  HostListener,
  inject,
  signal,
} from '@angular/core';

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
  private readonly router = inject(Router);

  private readonly currentUrl = toSignal(
    this.router.events.pipe(
      filter((e): e is NavigationEnd => e instanceof NavigationEnd),
      map(() => this.router.url),
      startWith(this.router.url),
    ),
    { initialValue: this.router.url },
  );

  readonly isProfileTabActive = computed(() => {
    const urlTree = this.router.parseUrl(this.currentUrl());
    const path = urlTree.root.children['primary']?.segments.map((s) => s.path).join('/') ?? '';
    return path === 'settings' && urlTree.queryParams['tab'] === 'profile';
  });

  readonly isSettingsTabActive = computed(() => {
    const urlTree = this.router.parseUrl(this.currentUrl());
    const path = urlTree.root.children['primary']?.segments.map((s) => s.path).join('/') ?? '';
    return path === 'settings' && urlTree.queryParams['tab'] !== 'profile';
  });

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
    {
      path: '/my-businesses',
      labelKey: 'NAV.MY_BUSINESSES',
      iconClass: 'fa-solid fa-store',
    },
  ];

  @HostListener('document:keydown.escape')
  onEsc(): void {
    this.menu.close();
  }
}
