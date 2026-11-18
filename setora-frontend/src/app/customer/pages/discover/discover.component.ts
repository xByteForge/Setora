import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { TranslocoModule } from '@ngneat/transloco';
import { BusinessDiscoveryService } from '../../../core/services/business-discovery.service';
import { BusinessListComponent } from './business-list/business-list.component';
import { PaginationComponent } from './pagination/pagination.component';
import { BusinessSearchBarComponent } from './business-search-bar/business-search-bar.component';
import { ResultsHeaderComponent } from './results-header/results-header.component';
import { ActiveFiltersChipsComponent } from './active-filters-chips/active-filters-chips.component';
import { FiltersPanelComponent } from './filters-panel/filters-panel.component';

@Component({
    selector: 'app-discover',
    standalone: true,
    imports: [
        BusinessListComponent,
        PaginationComponent,
        BusinessSearchBarComponent,
        ResultsHeaderComponent,
        ActiveFiltersChipsComponent,
        FiltersPanelComponent,
        TranslocoModule,
    ],
    templateUrl: './discover.component.html',
    styleUrl: './discover.component.css',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class DiscoverComponent {
    readonly service = inject(BusinessDiscoveryService);
    readonly filtersOpen = signal(false);

    onView(id: number) {
        // pagina de detalii a businessului nu exista inca
        console.log('view business', id);
    }
}