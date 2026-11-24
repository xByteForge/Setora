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
import { CalendarAppointment } from '../../../core/ui-models/calendar-appointment.model';
import { Employee } from '../../../core/models/employee.model';
import { businessDate, businessTime, toIsoDate } from '../../../core/utils/date.util';

@Component({
  selector: 'app-appointment-agenda',
  imports: [TranslocoModule],
  templateUrl: './appointment-agenda.component.html',
  styleUrl: './appointment-agenda.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AppointmentAgendaComponent {
  private readonly transloco = inject(TranslocoService);
  private readonly lang = toSignal(this.transloco.langChanges$, {
    initialValue: this.transloco.getActiveLang(),
  });

  readonly appointments = input.required<CalendarAppointment[]>();
  readonly employees = input.required<Employee[]>();
  readonly timezone = input.required<string>();
  readonly selectedId = input<number | null>(null);
  readonly selectedDate = model.required<Date>();
  readonly appointmentSelected = output<number>();

  readonly items = computed(() => {
    const iso = toIsoDate(this.selectedDate());
    const tz = this.timezone();
    return this.appointments()
      .filter((a) => businessDate(a.start, tz) === iso)
      .sort((a, b) => Date.parse(a.start) - Date.parse(b.start));
  });

  readonly dateLabel = computed(() =>
    new Intl.DateTimeFormat(this.lang(), { day: 'numeric', month: 'long', year: 'numeric' }).format(
      this.selectedDate(),
    ),
  );

  employeeName(id: number): string {
    return this.employees().find((e) => e.id === id)?.name ?? '';
  }

  timeLabel(a: CalendarAppointment): string {
    const tz = this.timezone();
    return `${businessTime(a.start, tz)}–${businessTime(a.end, tz)}`;
  }

  shift(direction: 1 | -1): void {
    const d = this.selectedDate();
    this.selectedDate.set(new Date(d.getFullYear(), d.getMonth(), d.getDate() + direction));
  }

  goToToday(): void {
    this.selectedDate.set(new Date());
  }

  select(a: CalendarAppointment): void {
    if (a.status === 'UNAVAILABLE') return;
    this.appointmentSelected.emit(a.id);
  }
}
