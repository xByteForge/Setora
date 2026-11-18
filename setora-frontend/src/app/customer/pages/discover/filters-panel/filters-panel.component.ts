import { ChangeDetectionStrategy, Component, HostListener, computed, inject, output, signal } from '@angular/core';
import { TranslocoModule } from '@ngneat/transloco';
import { BusinessDiscoveryService } from '../../../../core/services/business-discovery.service';
import { BusinessQuery } from '../../../../core/ui-models/business-query.model';
import { TIME_SLOTS, TIME_SLOT_KEYS, TimeSlot, slotOf } from '../../../../core/ui-models/time-slots.model';

interface Draft {
    category: string;
    serviceId: string;
    minPrice: string;
    maxPrice: string;
    date: string;
    time: TimeSlot;
}

@Component({
    selector: 'app-filters-panel',
    standalone: true,
    imports: [TranslocoModule],
    templateUrl: './filters-panel.component.html',
    styleUrl: './filters-panel.component.css',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FiltersPanelComponent {
    readonly service = inject(BusinessDiscoveryService);
    readonly closed = output<void>();
    readonly timeSlots = TIME_SLOT_KEYS;

    // ce a modificat utilizatorul si inca n-a aplicat (componenta se creeaza din nou la fiecare deschidere)
    private readonly edits = signal<Partial<Draft>>({});

    readonly draft = computed<Draft>(() => ({
        ...this.fromQuery(this.service.query()),
        ...this.edits(),
    }));

    // serviciile din categoria aleasa in formular
    readonly serviceOptions = computed(() =>
        this.service.servicesByCategory(this.draft().category || null),
    );

    // cate rezultate ar da filtrele din formular, inainte de a le aplica
    readonly previewCount = computed(() =>
        this.service.count({ ...this.service.query(), ...this.toPatch(this.draft()) }),
    );

    onField(key: keyof Draft, event: Event): void {
        const value = (event.target as HTMLInputElement | HTMLSelectElement).value;
        this.edits.update((e) => ({ ...e, [key]: value }));
    }

    onCategory(event: Event): void {
        const category = (event.target as HTMLSelectElement).value;
        const allowed = this.service.servicesByCategory(category || null);
        const current = this.draft().serviceId;
        const keepService = !current || allowed.some((s) => String(s.id) === current);
        this.edits.update((e) => ({ ...e, category, ...(keepService ? {} : { serviceId: '' }) }));
    }

    apply(): void {
        this.service.applyFilters(this.toPatch(this.draft()));
        this.edits.set({});
        this.closed.emit();
    }

    reset(): void {
        this.service.clearFilters();
        this.edits.set({});
    }

    @HostListener('document:keydown.escape')
    onEsc(): void {
        this.closed.emit();
    }

    private toPatch(d: Draft): Partial<BusinessQuery> {
        const [timeFrom, timeTo] = TIME_SLOTS[d.time];
        return {
            category: d.category || null,
            serviceId: d.serviceId ? Number(d.serviceId) : null,
            minPrice: d.minPrice === '' ? null : Number(d.minPrice),
            maxPrice: d.maxPrice === '' ? null : Number(d.maxPrice),
            date: d.date || null,
            timeFrom,
            timeTo,
        };
    }

    private fromQuery(q: BusinessQuery): Draft {
        return {
            category: q.category ?? '',
            serviceId: q.serviceId == null ? '' : String(q.serviceId),
            minPrice: q.minPrice == null ? '' : String(q.minPrice),
            maxPrice: q.maxPrice == null ? '' : String(q.maxPrice),
            date: q.date ?? '',
            time: slotOf(q.timeFrom, q.timeTo),
        };
    }
}