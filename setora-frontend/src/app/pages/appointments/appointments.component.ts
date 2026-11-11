import { ChangeDetectionStrategy, Component, signal } from '@angular/core';
import { TranslocoModule } from '@ngneat/transloco';
import { AppointmentsHeaderComponent } from './appointment-header/appointment-header.component';
import { AppointmentCalendarComponent } from './appointment-calendar/appointment-calendar.component';
import { AppointmentSummary } from '../../core/models/appointment-summary.model';

@Component({
  imports: [TranslocoModule, AppointmentsHeaderComponent, AppointmentCalendarComponent],
  selector: 'app-appointments',
  templateUrl: './appointments.component.html',
  styleUrl: './appointments.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AppointmentsComponent {
  readonly summary = signal<AppointmentSummary>({
    total: 9,
    confirmed: 7,
    pending: 1,
    cancelled: 1,
  });

  openAddAppointment(): void {
    // TODO: deschide formularul de adaugare
  }
}