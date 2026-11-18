import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { TranslocoModule } from '@ngneat/transloco';
import { BusinessDiscoveryService } from '../../../../core/services/business-discovery.service';
import { BusinessQuery } from '../../../../core/ui-models/business-query.model';
import { slotOf } from '../../../../core/ui-models/time-slots.model';

interface Chip {
    id: string;
    label?: string;                    // text brut (categorie, serviciu, dată)
    labelKey?: string;                 // cheie Transloco
    params?: Record<string, unknown>;
    clear: Partial<BusinessQuery>;     // ce se reseteaza cand apeși X
}

@Component({
    selector: 'app-active-filters-chips',
    standalone: true,
    imports: [TranslocoModule],
    templateUrl: './active-filters-chips.component.html',
    styleUrl: './active-filters-chips.component.css',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ActiveFiltersChipsComponent {
    readonly service = inject(BusinessDiscoveryService);

    readonly chips = computed<Chip[]>(() => {
        const q = this.service.query();
        const chips: Chip[] = [];

        const text = q.text.trim();
        if (text) {
            chips.push({ id: 'text', labelKey: 'DISCOVER.CHIPS.TEXT', params: { value: text }, clear: { text: '' } });
        }

        if (q.category) {
            chips.push({ id: 'category', label: q.category, clear: { category: null } });
        }

        if (q.serviceId != null) {
            const s = this.service.serviceOptions.find((o) => o.id === q.serviceId);
            if (s) chips.push({ id: 'service', label: s.name, clear: { serviceId: null } });
        }

        if (q.minPrice != null || q.maxPrice != null) {
            const key =
                q.minPrice != null && q.maxPrice != null
                    ? 'DISCOVER.CHIPS.PRICE_RANGE'
                    : q.minPrice != null
                        ? 'DISCOVER.CHIPS.PRICE_MIN'
                        : 'DISCOVER.CHIPS.PRICE_MAX';
            chips.push({
                id: 'price',
                labelKey: key,
                params: { min: q.minPrice, max: q.maxPrice },
                clear: { minPrice: null, maxPrice: null },
            });
        }

        if (q.date) {
            chips.push({ id: 'date', label: q.date.split('-').reverse().join('.'), clear: { date: null } });
        }

        const slot = slotOf(q.timeFrom, q.timeTo);
        if (slot !== 'any') {
            chips.push({ id: 'time', labelKey: 'DISCOVER.FILTERS.TIME.' + slot, clear: { timeFrom: null, timeTo: null } });
        }

        return chips;
    });

    remove(chip: Chip): void {
        this.service.applyFilters(chip.clear);
    }
}