import { ChangeDetectionStrategy, Component, computed, inject, input, model, output } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { TranslocoModule, TranslocoService } from '@ngneat/transloco';
import { CalendarAppointment } from '../../../core/models/calendar-appointment.model';
import { Employee } from '../../../core/models/employee.model';

const isSameDay = (a: Date, b: Date): boolean =>
  a.getFullYear() === b.getFullYear() &&
  a.getMonth() === b.getMonth() &&
  a.getDate() === b.getDate();

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
  readonly selectedId = input<string | null>(null);
  readonly selectedDate = model.required<Date>();
  readonly appointmentSelected = output<string>();

  readonly items = computed(() =>
    this.appointments()
      .filter((a) => isSameDay(new Date(a.start), this.selectedDate()))
      .sort((a, b) => a.start.localeCompare(b.start)),
  );

  readonly dateLabel = computed(() =>
    new Intl.DateTimeFormat(this.lang(), { day: 'numeric', month: 'long', year: 'numeric' }).format(
      this.selectedDate(),
    ),
  );

  employeeName(id: string): string {
    return this.employees().find((e) => e.id === id)?.name ?? '';
  }

  shift(direction: 1 | -1): void {
    const d = this.selectedDate();
    this.selectedDate.set(new Date(d.getFullYear(), d.getMonth(), d.getDate() + direction));
  }

  goToToday(): void {
    this.selectedDate.set(new Date());
  }

  select(a: CalendarAppointment): void {
    if (a.status === 'unavailable') return;
    this.appointmentSelected.emit(a.id);
  }
}