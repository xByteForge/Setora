import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { TranslocoModule } from '@ngneat/transloco';
import { BusinessDiscoveryService } from '../../../services/business-discovery.service';
import { BusinessSort } from '../../../ui-models/business-query.model';

@Component({
    selector: 'app-business-filters',
    standalone: true,
    imports: [TranslocoModule],
    templateUrl: './business-filters.component.html',
    styleUrl: './business-filters.component.css',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BusinessFiltersComponent {
    readonly service = inject(BusinessDiscoveryService);
    readonly sortOptions: BusinessSort[] = ['relevance', 'priceAsc', 'priceDesc', 'nameAsc'];

    onCity(event: Event): void {
        this.service.applyFilters({ city: (event.target as HTMLSelectElement).value });
    }

    onSort(event: Event): void {
        this.service.applyFilters({ sort: (event.target as HTMLSelectElement).value as BusinessSort });
    }
}