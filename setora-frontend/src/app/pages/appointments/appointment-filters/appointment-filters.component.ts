import { ChangeDetectionStrategy, Component, computed, input, model, signal } from '@angular/core';
import { TranslocoDirective } from '@ngneat/transloco';
import {
    LucideChevronDown,
    LucideClock,
    LucideListFilter,
    LucideMapPin,
    LucideUsers,
    LucideX,
} from '@lucide/angular';
import {
    AppointmentFilters,
    AppointmentStatus,
    FilterEmployee,
    FilterLocation,
    DEFAULT_FILTERS,
    DEFAULT_TIME_RANGE,
    STATUS_I18N_KEYS,
    TIME_PRESETS,
    TimeRange,
    minutesToLabel,
    rangeToLabel,
    sameRange,
} from '../../../core/models/appointment-filters.model';

type ChipKey = 'location' | 'employee' | 'status' | 'time';

interface FilterChip {
    key: ChipKey;
    text?: string;
    i18nKey?: string;
}

@Component({
    selector: 'app-appointment-filters',
    standalone: true,
    imports: [
        TranslocoDirective,
        LucideMapPin,
        LucideUsers,
        LucideListFilter,
        LucideClock,
        LucideChevronDown,
        LucideX,
    ],
    styleUrl: './appointment-filters.component.css',
    changeDetection: ChangeDetectionStrategy.OnPush,
    templateUrl: './appointment-filters.component.html',
})
export class AppointmentFiltersComponent {
    readonly locations = input.required<FilterLocation[]>();
    readonly employees = input.required<FilterEmployee[]>();

    /** Two-way: [(filters)]="filters" */
    readonly filters = model<AppointmentFilters>(DEFAULT_FILTERS);

    readonly presets = TIME_PRESETS;
    readonly statuses = Object.keys(STATUS_I18N_KEYS) as AppointmentStatus[];
    readonly statusKeys = STATUS_I18N_KEYS;
    readonly fromSlots = Array.from({ length: 48 }, (_, i) => i * 30);
    readonly toSlots = Array.from({ length: 48 }, (_, i) => (i + 1) * 30);
    readonly time = minutesToLabel;
    readonly rangeLabel = rangeToLabel;

    /** Doar angajații de la locația selectată. */
    readonly availableEmployees = computed(() => {
        const loc = this.filters().locationId;
        return loc ? this.employees().filter((e) => e.locationIds?.includes(loc) ?? true) : this.employees();
    });

    /** True după ce utilizatorul alege "Personalizat…", chiar dacă intervalul curent coincide cu o presetare. */
    private readonly customMode = signal(false);

    readonly presetValue = computed(() => {
        const i = this.presets.findIndex((p) => sameRange(p, this.filters().timeRange));
        return this.customMode() || i === -1 ? 'custom' : String(i);
    });

    /** Valorile selectate, afișate ca etichete ce pot fi eliminate. */
    readonly chips = computed<FilterChip[]>(() => {
        const f = this.filters();
        const chips: FilterChip[] = [];
        if (f.locationId) {
            chips.push({ key: 'location', text: this.locations().find((l) => l.id === f.locationId)?.name ?? '' });
        }
        if (f.employeeId) {
            chips.push({ key: 'employee', text: this.employees().find((e) => e.id === f.employeeId)?.name ?? '' });
        }
        if (f.status) {
            chips.push({ key: 'status', i18nKey: STATUS_I18N_KEYS[f.status] });
        }
        if (!sameRange(f.timeRange, DEFAULT_TIME_RANGE)) {
            chips.push({ key: 'time', text: rangeToLabel(f.timeRange) });
        }
        return chips;
    });

    onLocation(e: Event): void {
        const locationId = this.value(e) || null;
        const f = this.filters();
        // Dacă angajatul ales nu lucrează în noua locație, filtrul de angajat se resetează.
        const employeeStillValid =
            !f.employeeId ||
            !locationId ||
            (this.employees().find((x) => x.id === f.employeeId)?.locationIds?.includes(locationId) ?? true);
        this.filters.set({ ...f, locationId, employeeId: employeeStillValid ? f.employeeId : null });
    }

    onEmployee(e: Event): void {
        this.filters.update((f) => ({ ...f, employeeId: this.value(e) || null }));
    }

    onStatus(e: Event): void {
        this.filters.update((f) => ({ ...f, status: (this.value(e) || null) as AppointmentStatus | null }));
    }

    onPreset(e: Event): void {
        const v = this.value(e);
        if (v === 'custom') {
            this.customMode.set(true); // păstrează intervalul curent și arată selectoarele De la / Până la
            return;
        }
        this.customMode.set(false);
        this.filters.update((f) => ({ ...f, timeRange: this.presets[+v] }));
    }

    onCustom(edge: keyof TimeRange, e: Event): void {
        this.filters.update((f) => {
            const next = { ...f.timeRange, [edge]: +this.value(e) };
            // Intervalul rămâne valid: sfârșitul e mereu după început.
            if (next.from >= next.to) {
                if (edge === 'from') next.to = Math.min(next.from + 30, 24 * 60);
                else next.from = Math.max(next.to - 30, 0);
            }
            return { ...f, timeRange: next };
        });
    }

    clear(key: ChipKey): void {
        this.filters.update((f) => {
            switch (key) {
                case 'location': return { ...f, locationId: null };
                case 'employee': return { ...f, employeeId: null };
                case 'status': return { ...f, status: null };
                case 'time': this.customMode.set(false); return { ...f, timeRange: DEFAULT_TIME_RANGE };
            }
        });
    }

    resetAll(): void {
        this.customMode.set(false);
        this.filters.set(DEFAULT_FILTERS);
    }

    private value(e: Event): string {
        return (e.target as HTMLSelectElement).value;
    }
}