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
import { Employee, SalonLocation, AppointmentStatus } from '../../../core/models/calendar-appointment.model';
import {
    AppointmentFilters,
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
    template: `
    <section class="filters" *transloco="let t" role="search" [attr.aria-label]="t('APPOINTMENTS.FILTERS.TITLE')">
      <div class="row">
        <label class="field">
          <span class="sr-only">{{ t('APPOINTMENTS.FILTERS.LOCATION') }}</span>
          <svg lucideMapPin [size]="16" class="icon" aria-hidden="true"></svg>
          <select (change)="onLocation($event)">
            <option value="" [selected]="!filters().locationId">{{ t('APPOINTMENTS.FILTERS.ALL_LOCATIONS') }}</option>
            @for (l of locations(); track l.id) {
              <option [value]="l.id" [selected]="l.id === filters().locationId">{{ l.name }}</option>
            }
          </select>
          <svg lucideChevronDown [size]="16" class="chevron" aria-hidden="true"></svg>
        </label>

        <label class="field">
          <span class="sr-only">{{ t('APPOINTMENTS.FILTERS.EMPLOYEE') }}</span>
          <svg lucideUsers [size]="16" class="icon" aria-hidden="true"></svg>
          <select (change)="onEmployee($event)">
            <option value="" [selected]="!filters().employeeId">{{ t('APPOINTMENTS.FILTERS.ALL_EMPLOYEES') }}</option>
            @for (e of availableEmployees(); track e.id) {
              <option [value]="e.id" [selected]="e.id === filters().employeeId">{{ e.name }}</option>
            }
          </select>
          <svg lucideChevronDown [size]="16" class="chevron" aria-hidden="true"></svg>
        </label>

        <label class="field">
          <span class="sr-only">{{ t('APPOINTMENTS.FILTERS.STATUS') }}</span>
          <svg lucideListFilter [size]="16" class="icon" aria-hidden="true"></svg>
          <select (change)="onStatus($event)">
            <option value="" [selected]="!filters().status">{{ t('APPOINTMENTS.FILTERS.ALL_STATUSES') }}</option>
            @for (s of statuses; track s) {
              <option [value]="s" [selected]="s === filters().status">{{ t(statusKeys[s]) }}</option>
            }
          </select>
          <svg lucideChevronDown [size]="16" class="chevron" aria-hidden="true"></svg>
        </label>

        <label class="field">
          <span class="sr-only">{{ t('APPOINTMENTS.FILTERS.TIME_RANGE') }}</span>
          <svg lucideClock [size]="16" class="icon" aria-hidden="true"></svg>
          <select (change)="onPreset($event)">
            @for (p of presets; track $index) {
              <option [value]="$index" [selected]="presetValue() === '' + $index">{{ rangeLabel(p) }}</option>
            }
            <option value="custom" [selected]="presetValue() === 'custom'">{{ t('APPOINTMENTS.FILTERS.CUSTOM') }}</option>
          </select>
          <svg lucideChevronDown [size]="16" class="chevron" aria-hidden="true"></svg>
        </label>

        @if (presetValue() === 'custom') {
          <div class="custom-range" role="group" [attr.aria-label]="t('APPOINTMENTS.FILTERS.CUSTOM')">
            <label class="field field--compact">
              <span class="sr-only">{{ t('APPOINTMENTS.FILTERS.FROM') }}</span>
              <select (change)="onCustom('from', $event)">
                @for (m of fromSlots; track m) {
                  <option [value]="m" [selected]="m === filters().timeRange.from">{{ time(m) }}</option>
                }
              </select>
            </label>
            <span aria-hidden="true">–</span>
            <label class="field field--compact">
              <span class="sr-only">{{ t('APPOINTMENTS.FILTERS.TO') }}</span>
              <select (change)="onCustom('to', $event)">
                @for (m of toSlots; track m) {
                  <option [value]="m" [selected]="m === filters().timeRange.to">{{ time(m) }}</option>
                }
              </select>
            </label>
          </div>
        }
      </div>

      @if (chips().length) {
        <div class="active" aria-live="polite">
          <span class="active-title">{{ t('APPOINTMENTS.FILTERS.ACTIVE') }}</span>
          @for (c of chips(); track c.key) {
            @let label = c.i18nKey ? t(c.i18nKey) : c.text;
            <button
              type="button"
              class="chip"
              (click)="clear(c.key)"
              [attr.aria-label]="t('APPOINTMENTS.FILTERS.REMOVE', { filter: label })"
            >
              {{ label }}
              <svg lucideX [size]="12" aria-hidden="true"></svg>
            </button>
          }
          <button type="button" class="reset" (click)="resetAll()">
            {{ t('APPOINTMENTS.FILTERS.RESET') }}
          </button>
        </div>
      }
    </section>
  `,
})
export class AppointmentFiltersComponent {
    readonly locations = input.required<SalonLocation[]>();
    readonly employees = input.required<Employee[]>();

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
        return loc ? this.employees().filter((e) => e.locationIds.includes(loc)) : this.employees();
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
            !!this.employees().find((x) => x.id === f.employeeId)?.locationIds.includes(locationId);
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