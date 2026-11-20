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
import {
  LucideSearch,
  LucideSun,
  LucideMoon,
  LucideBell,
  LucideChevronRight,
} from '@lucide/angular';
import { ThemeService } from '../../services/theme.service';
import { BusinessService } from '../../services/business.service';
import { MobileMenuService } from '../../services/mobile-menu.service';
import { SearchService } from '../../services/search.service';
import { SearchResult } from '../../models/search-results.model';

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
        <div class="search hide-mobile">
          <svg lucideSearch [size]="16" aria-hidden="true"></svg>
          <input
            #q
            type="search"
            role="combobox"
            aria-autocomplete="list"
            aria-controls="search-panel"
            [attr.aria-expanded]="searchOpen()"
            [value]="query()"
            (input)="onQueryInput(q.value)"
            (keydown)="onSearchKeydown($event)"
            (focus)="searchOpen.set(query().trim().length > 0)"
            [placeholder]="t('HEADER.SEARCH')"
            [attr.aria-label]="t('HEADER.SEARCH')"
          />
          <kbd class="shortcut" aria-hidden="true">/</kbd>

          @if (searchOpen()) {
            <div class="search-panel" id="search-panel" role="listbox">
              @for (g of groups(); track g.category) {
                <div class="search-group">
                  <p class="group-title">{{ t(g.labelKey) }}</p>
                  @for (item of g.items; track item.id) {
                    <button
                      type="button"
                      role="option"
                      class="result"
                      [class.active]="flat()[activeIndex()] === item"
                      [attr.aria-selected]="flat()[activeIndex()] === item"
                      (mouseenter)="activeIndex.set(flat().indexOf(item))"
                      (click)="openResult(item)"
                    >
                      <span class="result-title">{{ item.title }}</span>
                      @if (item.subtitle) {
                        <span class="result-sub">{{ item.subtitle }}</span>
                      }
                    </button>
                  }
                </div>
              } @empty {
                <p class="no-results">{{ t('SEARCH.NO_RESULTS') }}</p>
              }
              <button type="button" class="see-all" (click)="seeAll()">
                {{ t('SEARCH.SEE_ALL') }} „{{ query().trim() }}"
              </button>
            </div>
          }
        </div>

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

        <button
          type="button"
          class="menu-btn"
          (click)="menu.toggle()"
          aria-haspopup="dialog"
          [attr.aria-expanded]="menu.open()"
        >
          {{ t('MENU.TITLE') }}
        </button>
      </div>
    </header>
  `,
})
export class TopbarComponent {
  readonly theme = inject(ThemeService);
  readonly menu = inject(MobileMenuService);
  private readonly transloco = inject(TranslocoService);
  private readonly businessService = inject(BusinessService);
  private readonly searchService = inject(SearchService);
  private readonly router = inject(Router);
  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef);

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

  // ---- search universal ----
  readonly query = signal('');
  readonly searchOpen = signal(false);
  readonly activeIndex = signal(0);
  readonly groups = computed(() => this.searchService.search(this.query()));
  readonly flat = computed(() => this.groups().flatMap((g) => g.items));

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

  onQueryInput(value: string): void {
    this.query.set(value);
    this.activeIndex.set(0);
    this.searchOpen.set(value.trim().length > 0);
  }

  onSearchKeydown(e: KeyboardEvent): void {
    const n = this.flat().length;

    if (e.key === 'ArrowDown' && n > 0) {
      e.preventDefault();
      this.activeIndex.update((i) => (i + 1) % n);
    } else if (e.key === 'ArrowUp' && n > 0) {
      e.preventDefault();
      this.activeIndex.update((i) => (i - 1 + n) % n);
    } else if (e.key === 'Enter') {
      e.preventDefault();
      const item = this.flat()[this.activeIndex()];
      if (item) this.openResult(item);
      else if (this.query().trim()) this.seeAll();
    }
  }

  openResult(item: SearchResult): void {
    this.router.navigateByUrl(item.url);
    this.closeSearch();
  }

  seeAll(): void {
    const q = this.query().trim();
    if (!q) return;
    this.router.navigate(['/discover'], { queryParams: { q } });
    this.closeSearch();
  }

  private closeSearch(): void {
    this.searchOpen.set(false);
    this.query.set('');
    this.searchInput()?.nativeElement.blur();
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
      this.searchOpen.set(false);
    }
  }

  @HostListener('document:keydown.escape')
  onEsc(): void {
    this.langMenuOpen.set(false);
    if (this.searchOpen()) this.closeSearch();
  }

  // scurtaturi: "/" sau Ctrl/Cmd+K focuseaza search-ul
  @HostListener('document:keydown', ['$event'])
  onGlobalKey(e: KeyboardEvent): void {
    const target = e.target as HTMLElement;
    const typing = /^(input|textarea|select)$/i.test(target.tagName) || target.isContentEditable;

    const isCmdK = (e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'k';
    const isSlash = e.key === '/' && !typing;

    if (isCmdK || isSlash) {
      e.preventDefault();
      this.searchInput()?.nativeElement.focus();
    }
  }
}
