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
} from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink } from '@angular/router';
import { filter, map, startWith } from 'rxjs';
import { TranslocoDirective, TranslocoService } from '@ngneat/transloco';
import {
  LucideSearch,
  LucideSun,
  LucideMoon,
  LucideBell,
  LucideChevronRight,
} from '@lucide/angular';
import { ThemeService } from '../../services/theme.service';
import { BusinessService } from '../../services/business.service';

export interface Breadcrumb {
  label?: string;     // text brut (ex: numele business-ului)
  labelKey?: string;  // cheie Transloco (ex: 'NAV.APPOINTMENTS')
  url?: string;
}

@Component({
  selector: 'app-topbar',
  standalone: true,
  imports: [
    RouterLink,
    TranslocoDirective,
    LucideSearch,
    LucideSun,
    LucideMoon,
    LucideBell,
    LucideChevronRight,
  ],
  styleUrl: './topbar.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <header class="topbar" *transloco="let t">
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

      <div class="actions">
        <form class="search" role="search" (submit)="$event.preventDefault(); onSearch(q.value)">
          <svg lucideSearch [size]="16" aria-hidden="true"></svg>
          <input
            #q
            type="search"
            [placeholder]="t('HEADER.SEARCH')"
            [attr.aria-label]="t('HEADER.SEARCH')"
          />
        </form>

        <button
          type="button"
          class="icon-btn"
          (click)="theme.toggleTheme()"
          [attr.aria-label]="
            theme.currentTheme() === 'light' ? t('HEADER.THEME_TO_DARK') : t('HEADER.THEME_TO_LIGHT')
          "
        >
          @if (theme.currentTheme() === 'light') {
            <svg lucideSun [size]="20" aria-hidden="true"></svg>
          } @else {
            <svg lucideMoon [size]="20" aria-hidden="true"></svg>
          }
        </button>

        <button
          type="button"
          class="icon-btn"
          (click)="notificationsClicked.emit()"
          [attr.aria-label]="t('HEADER.NOTIFICATIONS')"
        >
          <svg lucideBell [size]="20" aria-hidden="true"></svg>
          @if (unreadCount() > 0) {
            <span class="dot" aria-hidden="true"></span>
          }
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
      </div>
    </header>
  `,
})
export class TopbarComponent {
  readonly theme = inject(ThemeService);
  private readonly transloco = inject(TranslocoService);
  private readonly businessService = inject(BusinessService);
  private readonly router = inject(Router);
  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef);

  readonly unreadCount = input(0);
  readonly searched = output<string>();
  readonly notificationsClicked = output<void>();

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
    return [
      ...(business ? [{ label: business.name, url: '/' }] : []),
      ...this.routeCrumbs(),
    ];
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

  onSearch(value: string): void {
    const q = value.trim();
    if (q) this.searched.emit(q);
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