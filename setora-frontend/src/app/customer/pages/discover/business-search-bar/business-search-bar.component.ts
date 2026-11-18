import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { TranslocoModule } from '@ngneat/transloco';
import { LucideSearch } from '@lucide/angular';
import { BusinessDiscoveryService } from '../../../../core/services/business-discovery.service';

@Component({
    selector: 'app-business-search-bar',
    standalone: true,
    imports: [TranslocoModule, LucideSearch],
    templateUrl: './business-search-bar.component.html',
    styleUrl: './business-search-bar.component.css',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BusinessSearchBarComponent {
    private readonly service = inject(BusinessDiscoveryService);

    readonly text = computed(() => this.service.query().text);

    onSubmit(value: string): void {
        this.service.applyFilters({ text: value.trim() });
    }
}