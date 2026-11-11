import { ChangeDetectionStrategy, Component, computed, inject, input, model } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { TranslocoModule, TranslocoService } from '@ngneat/transloco';
import type { CalendarService } from '../../../core/services/calendar.service';
import { CalendarView } from '../../../core/models/calendar-view.type';
import { CalendarAppointment } from '../../../core/models/calendar-appointment.model';
import { TimeRange } from '../../../core/models/appointment-filters.model';
import { CalendarAppointmentBlockComponent } from '../calendar-appointment-block/calendar-appointment-block.component';
import { CalendarToolbarComponent } from '../calendar-toolbar/calendar-toolbar.component';
import { layoutOverlaps, PositionedAppointment } from './calendar-layout';

const PX_PER_MINUTE = 1;

/** Tipul angajatului, luat direct din serviciu (nu trebuie să-l redefinim). */
type CalendarEmployee = ReturnType<CalendarService['getEmployees']>[number];

interface CalendarColumn {
    id: string;
    initials: string;
    title: string;
    subtitle?: string;
    count: number;
    appointments: PositionedAppointment[];
}

const isSameDay = (a: Date, b: Date): boolean =>
    a.getFullYear() === b.getFullYear() &&
    a.getMonth() === b.getMonth() &&
    a.getDate() === b.getDate();

const countReal = (items: CalendarAppointment[]): number =>
    items.filter((a) => a.status !== 'unavailable').length;

@Component({
    selector: 'app-appointment-calendar',
    imports: [TranslocoModule, CalendarAppointmentBlockComponent, CalendarToolbarComponent],
    templateUrl: './appointment-calendar.component.html',
    styleUrl: './appointment-calendar.component.css',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AppointmentCalendarComponent {
    private readonly transloco = inject(TranslocoService);

    private readonly lang = toSignal(this.transloco.langChanges$, {
        initialValue: this.transloco.getActiveLang(),
    });

    /** Date deja filtrate de pagină. */
    readonly employees = input.required<CalendarEmployee[]>();
    readonly appointments = input.required<CalendarAppointment[]>();
    readonly timeRange = input.required<TimeRange>();

    /** Vederea pe săptămână arată un singur angajat: primul din lista filtrată. */
    readonly selectedEmployeeId = computed(() => this.employees()[0]?.id ?? '');

    /** Two-way cu pagina, ca sumarul să numere ziua afișată. */
    readonly selectedDate = model(new Date(2026, 10, 7));
    /** Two-way cu pagina, ca filtrele să știe ce vedere e activă. */
    readonly view = model<CalendarView>('day');

    readonly pxPerMinute = PX_PER_MINUTE;
    readonly hourHeight = 60 * PX_PER_MINUTE;

    readonly startHour = computed(() => Math.floor(this.timeRange().from / 60));
    readonly endHour = computed(() => Math.ceil(this.timeRange().to / 60));
    readonly hours = computed(() =>
        Array.from({ length: Math.max(this.endHour() - this.startHour(), 0) }, (_, i) => this.startHour() + i),
    );
    readonly gridHeight = computed(() => (this.endHour() - this.startHour()) * this.hourHeight);

    /** Luni–Duminica pentru saptamana datei selectate */
    readonly weekDays = computed(() => {
        const d = this.selectedDate();
        const offset = (d.getDay() + 6) % 7; // luni = 0
        return Array.from(
            { length: 7 },
            (_, i) => new Date(d.getFullYear(), d.getMonth(), d.getDate() - offset + i),
        );
    });

    readonly dateLabel = computed(() => {
        const format = new Intl.DateTimeFormat(this.lang(), {
            day: 'numeric',
            month: 'long',
            year: 'numeric',
        });
        if (this.view() === 'week') {
            const days = this.weekDays();
            return format.formatRange(days[0], days[6]);
        }
        return format.format(this.selectedDate());
    });

    readonly weekday = computed(() =>
        this.selectedDate()
            .toLocaleDateString(this.lang(), { weekday: 'short' })
            .replace('.', '')
            .toUpperCase(),
    );

    readonly day = computed(() => String(this.selectedDate().getDate()).padStart(2, '0'));

    readonly columns = computed<CalendarColumn[]>(() => {
        const all = this.appointments();

        if (this.view() === 'week') {
            return this.weekDays().map((date) => {
                const items = all.filter(
                    (a) =>
                        a.employeeId === this.selectedEmployeeId() &&
                        isSameDay(new Date(a.start), date),
                );
                return {
                    id: date.toISOString(),
                    initials: String(date.getDate()).padStart(2, '0'),
                    title: date.toLocaleDateString(this.lang(), { weekday: 'long' }),
                    count: countReal(items),
                    appointments: layoutOverlaps(items),
                };
            });
        }

        return this.employees().map((employee) => {
            const items = all.filter(
                (a) => a.employeeId === employee.id && isSameDay(new Date(a.start), this.selectedDate()),
            );
            return {
                id: employee.id,
                initials: employee.initials,
                title: employee.name,
                subtitle: employee.role,
                count: countReal(items),
                appointments: layoutOverlaps(items),
            };
        });
    });

    previous(): void {
        this.shift(-1);
    }

    next(): void {
        this.shift(1);
    }

    goToToday(): void {
        this.selectedDate.set(new Date());
    }

    setView(view: CalendarView): void {
        this.view.set(view);
    }

    formatHour(hour: number): string {
        return `${String(hour).padStart(2, '0')}:00`;
    }

    private shift(direction: 1 | -1): void {
        const step = this.view() === 'week' ? 7 : 1;
        const d = this.selectedDate();
        this.selectedDate.set(new Date(d.getFullYear(), d.getMonth(), d.getDate() + step * direction));
    }
}