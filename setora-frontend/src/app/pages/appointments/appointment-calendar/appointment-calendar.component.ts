import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { TranslocoModule, TranslocoService } from '@ngneat/transloco';
import { CalendarService } from '../../../core/services/calendar.service';
import { CalendarView } from '../../../core/models/calendar-view.type';
import { CalendarAppointment } from '../../../core/models/calendar-appointment.model';
import { CalendarAppointmentBlockComponent } from '../calendar-appointment-block/calendar-appointment-block.component';
import { CalendarToolbarComponent } from '../calendar-toolbar/calendar-toolbar.component';
import { layoutOverlaps, PositionedAppointment } from './calendar-layout';

const START_HOUR = 9;
const END_HOUR = 17;
const PX_PER_MINUTE = 1;

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
    items.filter((a) => a.status !== 'blocked').length;

@Component({
    selector: 'app-appointment-calendar',
    imports: [TranslocoModule, CalendarAppointmentBlockComponent, CalendarToolbarComponent],
    templateUrl: './appointment-calendar.component.html',
    styleUrl: './appointment-calendar.component.css',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AppointmentCalendarComponent {
    private readonly calendarService = inject(CalendarService);
    private readonly transloco = inject(TranslocoService);

    private readonly lang = toSignal(this.transloco.langChanges$, {
        initialValue: this.transloco.getActiveLang(),
    });

    readonly employees = signal(this.calendarService.getEmployees());
    readonly selectedEmployeeId = signal(this.employees()[0]?.id ?? '');
    readonly appointments = signal(this.calendarService.getAppointments());
    readonly selectedDate = signal(new Date(2026, 10, 7));
    readonly view = signal<CalendarView>('day');

    readonly startHour = START_HOUR;
    readonly pxPerMinute = PX_PER_MINUTE;
    readonly hourHeight = 60 * PX_PER_MINUTE;
    readonly gridHeight = (END_HOUR - START_HOUR) * this.hourHeight;
    readonly hours = Array.from({ length: END_HOUR - START_HOUR }, (_, i) => START_HOUR + i);

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

    setEmployee(id: string): void {
        this.selectedEmployeeId.set(id);
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