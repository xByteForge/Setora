import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  HostListener,
  computed,
  inject,
  input,
  output,
  signal,
  viewChild,
} from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink } from '@angular/router';
import { filter, map, startWith } from 'rxjs';
import { TranslocoDirective, TranslocoService } from '@ngneat/transloco';
import { LucideArrowLeft, LucideBell, LucideChevronRight, LucideLogOut, LucideMoon, LucideSun } from '@lucide/angular';
import { ThemeService } from '../../services/theme.service';
import { BusinessService } from '../../services/business.service';
import { MobileMenuService } from '../../services/mobile-menu.service';
import { UniversalSearchComponent } from '../universal-search/universal-search.component';
import { AuthService } from '../../services/auth.service';

export interface Breadcrumb {
  label?: string; // text brut (ex: numele business-ului)
  labelKey?: string; // cheie Transloco (ex: 'NAV.APPOINTMENTS')
  url?: string;
}

@Component({
  selector: 'app-topbar',
  standalone: true,
  imports: [
    RouterLink,
    TranslocoDirective,
    LucideArrowLeft,
    LucideSun,
    LucideMoon,
    LucideBell,
    LucideChevronRight,
    UniversalSearchComponent,
    LucideLogOut,
  ],
  styleUrl: './topbar.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <header class="topbar" *transloco="let t">
      @if (hideSidebar()) {
        <a routerLink="/discover" class="brand" aria-label="SETORA">
          <span class="brand-plaque">
            <img src="assets/logo.jpg" alt="" class="brand-logo" />
          </span>
          <span class="brand-name">SETORA</span>
        </a>
      } @else {
        <nav class="breadcrumb" [attr.aria-label]="t('HEADER.BREADCRUMB')">
          <ol>
            @for (item of items(); track $index; let last = $last) {
              <li>
                @if (item.url && !last) {
                  <a [routerLink]="item.url">{{ item.labelKey ? t(item.labelKey) : item.label }}</a>
                } @else {
                  <span [attr.aria-current]="last ? 'page' : null" [class.current]="last">
                    {{ item.labelKey ? t(item.labelKey) : item.label }}
                  </span>
                }
                @if (!last) {
                  <svg lucideChevronRight [size]="16" aria-hidden="true"></svg>
                }
              </li>
            }
          </ol>
        </nav>
      }

      <div class="actions">
        @if (backUrl() !== null) {
          @if (backUrl()) {
            <a [routerLink]="backUrl()" class="back-btn">
              <svg lucideArrowLeft [size]="16" aria-hidden="true"></svg>
              <span>{{ t(backLabelKey() ?? 'BUSINESS_REGISTRATION.BACK') }}</span>
            </a>
          } @else {
            <button type="button" class="back-btn">
              <svg lucideArrowLeft [size]="16" aria-hidden="true"></svg>
              <span>{{ t(backLabelKey() ?? 'BUSINESS_REGISTRATION.BACK') }}</span>
            </button>
          }
        } @else {
          <app-universal-search class="hide-mobile" />
        }

        <button
          type="button"
          class="icon-btn hide-mobile"
          (click)="theme.toggleTheme()"
          [attr.aria-label]="
            theme.currentTheme() === 'light'
              ? t('HEADER.THEME_TO_DARK')
              : t('HEADER.THEME_TO_LIGHT')
          "
        >
          @if (theme.currentTheme() === 'light') {
            <svg lucideSun [size]="20" aria-hidden="true"></svg>
          } @else {
            <svg lucideMoon [size]="20" aria-hidden="true"></svg>
          }
        </button>

        @if (backUrl() === null) {
          <button
            type="button"
            class="icon-btn hide-mobile"
            (click)="notificationsClicked.emit()"
            [attr.aria-label]="t('HEADER.NOTIFICATIONS')"
          >
            <svg lucideBell [size]="20" aria-hidden="true"></svg>
            @if (unreadCount() > 0) {
              <span class="dot" aria-hidden="true"></span>
            }
          </button>
        }

        <button
          type="button"
          class="icon-btn hide-mobile"
          (click)="auth.logout()"
          [attr.aria-label]="t('HEADER.LOGOUT')"
          [attr.title]="t('HEADER.LOGOUT')"
        >
          <svg lucideLogOut [size]="20" aria-hidden="true"></svg>
        </button>

        <div class="lang">
          <button
            type="button"
            class="lang-btn"
            aria-haspopup="listbox"
            [attr.aria-expanded]="langMenuOpen()"
            [attr.aria-label]="t('HEADER.LANGUAGE')"
            (click)="langMenuOpen.set(!langMenuOpen())"
          >
            {{ activeLang().toUpperCase() }}
          </button>
          @if (langMenuOpen()) {
            <ul class="lang-menu" role="listbox">
              @for (l of languages; track l) {
                <li role="option" [attr.aria-selected]="l === activeLang()">
                  <button type="button" (click)="setLang(l)">{{ l.toUpperCase() }}</button>
                </li>
              }
            </ul>
          }
        </div>

        @if (!hideSidebar()) {
          <button
            type="button"
            class="menu-btn"
            (click)="menu.toggle()"
            aria-haspopup="dialog"
            [attr.aria-expanded]="menu.open()"
          >
            {{ t('MENU.TITLE') }}
          </button>
        }
      </div>
    </header>
  `,
})
export class TopbarComponent {
  readonly theme = inject(ThemeService);
  readonly menu = inject(MobileMenuService);
  private readonly transloco = inject(TranslocoService);
  private readonly businessService = inject(BusinessService);
  private readonly router = inject(Router);
  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef);
  readonly auth = inject(AuthService);

  readonly hideSidebar = input(false);
  readonly backUrl = input<string | null>(null);
  readonly backLabelKey = input<string | null>(null);
  readonly unreadCount = input(0);
  readonly notificationsClicked = output<void>();

  readonly searchInput = viewChild<ElementRef<HTMLInputElement>>('q');

  // paginile din rutele active, recalculate la fiecare navigare
  private readonly routeCrumbs = toSignal(
    this.router.events.pipe(
      filter((e) => e instanceof NavigationEnd),
      startWith(null),
      map(() => this.buildCrumbs()),
    ),
    { initialValue: [] as Breadcrumb[] },
  );

  // numele business-ului + paginile curente
  protected readonly items = computed<Breadcrumb[]>(() => {
    const business = this.businessService.current();
    return [...(business ? [{ label: business.name, url: '/' }] : []), ...this.routeCrumbs()];
  });

  readonly languages = ['ro', 'en'];
  readonly langMenuOpen = signal(false);
  readonly activeLang = signal(this.transloco.getActiveLang());

  private buildCrumbs(): Breadcrumb[] {
    const crumbs: Breadcrumb[] = [];
    let snapshot = this.router.routerState.snapshot.root;
    let url = '';

    while (snapshot.firstChild) {
      snapshot = snapshot.firstChild;
      const segment = snapshot.url.map((s) => s.path).join('/');
      if (segment) url += '/' + segment;

      const key = snapshot.data['breadcrumb'] as string | undefined;
      if (key) crumbs.push({ labelKey: key, url });
    }
    return crumbs;
  }

  setLang(lang: string): void {
    this.transloco.setActiveLang(lang);
    this.activeLang.set(lang);
    this.langMenuOpen.set(false);
  }

  @HostListener('document:click', ['$event'])
  onDocClick(e: MouseEvent): void {
    if (!this.host.nativeElement.contains(e.target as Node)) {
      this.langMenuOpen.set(false);
    }
  }

  @HostListener('document:keydown.escape')
  onEsc(): void {
    this.langMenuOpen.set(false);
  }
}
