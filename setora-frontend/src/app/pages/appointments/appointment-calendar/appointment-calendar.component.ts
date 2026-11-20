import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  input,
  model,
  output,
} from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { TranslocoModule, TranslocoService } from '@ngneat/transloco';
import { CalendarView } from '../../../core/ui-models/calendar-view.type';
import { CalendarAppointment } from '../../../core/ui-models/calendar-appointment.model';
import { TimeRange } from '../../../core/ui-models/appointment-filters.model';
import { Employee } from '../../../core/models/employee.model';
import { businessDate, toIsoDate } from '../../../core/utils/date.util';
import { CalendarAppointmentBlockComponent } from '../calendar-appointment-block/calendar-appointment-block.component';
import { CalendarToolbarComponent } from '../calendar-toolbar/calendar-toolbar.component';
import { layoutOverlaps, PositionedAppointment } from './calendar-layout';

const PX_PER_MINUTE = 1;

interface CalendarColumn {
  id: string | number;
  initials: string;
  title: string;
  subtitle?: string;
  count: number;
  appointments: PositionedAppointment[];
}

/** "Ana Ionescu" -> "AI" */
const initialsOf = (name: string): string =>
  name
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((p) => p[0].toUpperCase())
    .join('');

const countReal = (items: CalendarAppointment[]): number =>
  items.filter((a) => a.status !== 'UNAVAILABLE').length;

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

  readonly employees = input.required<Employee[]>();
  readonly appointments = input.required<CalendarAppointment[]>();
  readonly timeRange = input.required<TimeRange>();
  readonly timezone = input.required<string>();

  /** Vederea pe saptamana arata un singur angajat: primul din lista filtrata. */
  readonly selectedEmployeeId = computed(() => this.employees()[0]?.id ?? null);

  /** Two-way cu pagina, ca sumarul sa numere ziua afisata. */
  readonly selectedDate = model(new Date(2026, 10, 23));
  /** Two-way cu pagina, ca filtrele sa stie ce vedere e activa. */
  readonly view = model<CalendarView>('day');

  readonly pxPerMinute = PX_PER_MINUTE;
  readonly hourHeight = 60 * PX_PER_MINUTE;

  readonly startHour = computed(() => Math.floor(this.timeRange().from / 60));
  readonly endHour = computed(() => Math.ceil(this.timeRange().to / 60));
  readonly hours = computed(() =>
    Array.from(
      { length: Math.max(this.endHour() - this.startHour(), 0) },
      (_, i) => this.startHour() + i,
    ),
  );
  readonly gridHeight = computed(() => (this.endHour() - this.startHour()) * this.hourHeight);

  /** Luni–Duminica pentru saptamana datei selectate. */
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
    const tz = this.timezone();

    if (this.view() === 'week') {
      return this.weekDays().map((date) => {
        const iso = toIsoDate(date);
        const items = all.filter(
          (a) => a.employeeId === this.selectedEmployeeId() && businessDate(a.start, tz) === iso,
        );
        return {
          id: iso,
          initials: String(date.getDate()).padStart(2, '0'),
          title: date.toLocaleDateString(this.lang(), { weekday: 'long' }),
          count: items.length,
          appointments: layoutOverlaps(items),
        };
      });
    }

    const iso = toIsoDate(this.selectedDate());
    return this.employees().map((employee) => {
      const items = all.filter(
        (a) => a.employeeId === employee.id && businessDate(a.start, tz) === iso,
      );
      return {
        id: employee.id,
        initials: initialsOf(employee.name),
        title: employee.name,
        subtitle: employee.employmentRole,
        count: items.length,
        appointments: layoutOverlaps(items),
      };
    });
  });

  readonly selectedId = input<number | null>(null);
  readonly appointmentSelected = output<number>();

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

  select(a: CalendarAppointment): void {
    this.appointmentSelected.emit(a.id);
  }
}
