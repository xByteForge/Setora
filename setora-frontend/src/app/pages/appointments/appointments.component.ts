import { ChangeDetectionStrategy, Component, computed, signal } from '@angular/core';
import { TranslocoModule } from '@ngneat/transloco';
import { AppointmentsHeaderComponent } from './appointment-header/appointment-header.component';
import { AppointmentFiltersComponent } from './appointment-filters/appointment-filters.component';
import { AppointmentSummary } from '../../core/models/appointment-summary.model';
import { AppointmentFilters, DEFAULT_FILTERS } from '../../core/models/appointment-filters.model';
import {
  filterAppointments,
  filterEmployees,
  visibleHours,
} from './appointment-filters/appointment-filters.util';
import { MOCK_APPOINTMENTS, MOCK_EMPLOYEES, MOCK_LOCATIONS } from './appointments.mock';

@Component({
  imports: [TranslocoModule, AppointmentsHeaderComponent, AppointmentFiltersComponent],
  selector: 'app-appointments',
  styleUrl: './appointments.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <app-appointments-header
      [summary]="summary()"
      (addClicked)="openAddAppointment()"
    />

    <app-appointment-filters
      [locations]="locations()"
      [employees]="employees()"
      [(filters)]="filters"
    />

    <!-- Calendarul primește doar date deja filtrate:
         [employees]="visibleEmployees()" [appointments]="visibleAppointments()" [hours]="visibleHours()" -->
  `,
})
export class AppointmentsComponent {
  // TODO: înlocuiește mock-urile cu date din serviciu.
  readonly locations = signal(MOCK_LOCATIONS);
  readonly employees = signal(MOCK_EMPLOYEES);
  readonly appointments = signal(MOCK_APPOINTMENTS);

  readonly filters = signal<AppointmentFilters>(DEFAULT_FILTERS);

  // Se recalculează automat la orice schimbare de filtru.
  readonly visibleAppointments = computed(() => filterAppointments(this.appointments(), this.filters()));
  readonly visibleEmployees = computed(() => filterEmployees(this.employees(), this.filters()));
  readonly visibleHours = computed(() => visibleHours(this.filters()));

  /**
   * Cardurile de sumar țin cont de locație, angajat și interval orar, dar nu de filtrul
   * de status (altfel celelalte carduri ar arăta 0). Pauzele ("blocked") nu se numără.
   */
  readonly summary = computed<AppointmentSummary>(() => {
    const items = filterAppointments(this.appointments(), { ...this.filters(), status: null });
    const count = (s: string) => items.filter((a) => a.status === s).length;
    const confirmed = count('confirmed');
    const pending = count('pending');
    const cancelled = count('cancelled');
    return { total: confirmed + pending + cancelled, confirmed, pending, cancelled };
  });

  openAddAppointment(): void {
    // TODO: deschide formularul de adaugare
  }
}