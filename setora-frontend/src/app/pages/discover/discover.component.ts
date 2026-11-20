import { ChangeDetectionStrategy, Component, effect, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { TranslocoModule } from '@ngneat/transloco';
import { BusinessDiscoveryService } from '../../core/services/business-discovery.service';
import { BusinessListComponent } from './business-list/business-list.component';
import { PaginationComponent } from './pagination/pagination.component';
import { ResultsHeaderComponent } from './results-header/results-header.component';
import { ActiveFiltersChipsComponent } from './active-filters-chips/active-filters-chips.component';
import { FiltersPanelComponent } from './filters-panel/filters-panel.component';
import { BusinessQuery } from '../../core/ui-models/business-query.model';
import { UniversalSearchComponent } from "../../core/layout/universal-search/universal-search.component";

@Component({
    selector: 'app-discover',
    standalone: true,
    imports: [
        BusinessListComponent,
        PaginationComponent,
        ResultsHeaderComponent,
        ActiveFiltersChipsComponent,
        FiltersPanelComponent,
        TranslocoModule,
        UniversalSearchComponent,
    ],
    templateUrl: './discover.component.html',
    styleUrl: './discover.component.css',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class DiscoverComponent {
    readonly service = inject(BusinessDiscoveryService);
    private readonly route = inject(ActivatedRoute);
    private readonly router = inject(Router);
    readonly filtersOpen = signal(false);
    readonly sortOptions = ['relevance', 'priceAsc', 'priceDesc', 'nameAsc'] as const;

    constructor() {
        // search-ul universal din topbar trimite textul prin ?q=
        this.route.queryParamMap
            .pipe(takeUntilDestroyed())
            .subscribe(params => {
                const q = params.get('q') ?? '';
                if (q !== this.service.query().text) this.service.applyFilters({ text: q });
            });

        // cand textul e sters din chip (sau din „Șterge filtrele"), scoate si ?q= din URL,
        // ca o cautare noua cu acelasi text sa functioneze din nou
        effect(() => {
            const text = this.service.query().text;
            if (text === '' && this.route.snapshot.queryParamMap.has('q')) {
                this.router.navigate([], {
                    queryParams: { q: null },
                    queryParamsHandling: 'merge',
                    replaceUrl: true,
                });
            }
        });
    }

    onView(id: number) {
        // pagina de detalii a businessului nu exista inca
        console.log('view business', id);
    }

    setCity(value: string): void {
        this.service.applyFilters({ city: value });
    }

    setSort(value: string): void {
        this.service.applyFilters({ sort: value as BusinessQuery['sort'] });
    }
}