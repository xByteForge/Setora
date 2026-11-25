import { Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { filter, map, startWith } from 'rxjs';
import { SidebarComponent } from './core/layout/sidebar/sidebar.component';
import { ThemeService } from './core/services/theme.service';
import { TopbarComponent } from './core/layout/topbar/topbar.component';
import { BusinessService } from './core/services/business.service';
import { CatalogService } from './core/services/catalog.service';

@Component({
  imports: [RouterOutlet, SidebarComponent, TopbarComponent],
  selector: 'app-root',
  styleUrl: './app.css',
  templateUrl: './app.html',
})
export class App {
  protected readonly title = signal('setora-frontend');
  private readonly themeService = inject(ThemeService);
  private readonly businessService = inject(BusinessService);
  private readonly catalog = inject(CatalogService);
  private readonly router = inject(Router);

  private readonly routeData = toSignal(
    this.router.events.pipe(
      filter((e) => e instanceof NavigationEnd),
      startWith(null),
      map(() => {
        let snapshot = this.router.routerState.snapshot.root;
        while (snapshot.firstChild) snapshot = snapshot.firstChild;
        return snapshot.data;
      }),
    ),
    { initialValue: {} as Record<string, unknown> },
  );

  protected readonly hideSidebar = computed(() => Boolean(this.routeData()['hideSidebar']));
  protected readonly topbarBackUrl = computed(
    () => (this.routeData()['topbarBackUrl'] as string | undefined) ?? null,
  );
  protected readonly topbarBackLabelKey = computed(
    () => (this.routeData()['topbarBackLabelKey'] as string | undefined) ?? null,
  );

  constructor() {
    // Un singur loc pentru datele mock ale businessului: CatalogService.
    this.businessService.setCurrent(this.catalog.getBusiness());
  }
}
