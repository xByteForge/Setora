import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { TranslocoModule } from '@ngneat/transloco';
import { AppointmentsHeaderComponent } from './appointment-header/appointment-header.component';
import { AppointmentFiltersComponent } from './appointment-filters/appointment-filters.component';
import { AppointmentCalendarComponent } from './appointment-calendar/appointment-calendar.component';
import { AppointmentSummary } from '../../core/models/appointment-summary.model';
import { AppointmentFilters, DEFAULT_FILTERS } from '../../core/models/appointment-filters.model';
import { CalendarView } from '../../core/models/calendar-view.type';
import { CalendarService } from '../../core/services/calendar.service';
import { filterAppointments, filterEmployees, isSameDay } from './appointment-filters/appointment-filters.util';
import { MOCK_LOCATIONS } from './appointments.mock';

@Component({
  imports: [TranslocoModule, AppointmentsHeaderComponent, AppointmentCalendarComponent, AppointmentFiltersComponent],
  selector: 'app-appointments',
  templateUrl: './appointments.component.html',
  styleUrl: './appointments.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AppointmentsComponent {
  private readonly calendarService = inject(CalendarService);

  // Pagina deține datele; calendarul doar le afișează.
  readonly locations = signal(MOCK_LOCATIONS); // TODO: din serviciu
  readonly employees = signal(this.calendarService.getEmployees());
  readonly appointments = signal(this.calendarService.getAppointments());

  readonly filters = signal<AppointmentFilters>(DEFAULT_FILTERS);
  /** Ziua afișată în calendar (sincronizată cu acesta). */
  readonly selectedDate = signal(new Date(2026, 10, 7));
  readonly view = signal<CalendarView>('day');

  // Se recalculează automat la orice schimbare de filtru.
  readonly visibleAppointments = computed(() => filterAppointments(this.appointments(), this.filters()));
  readonly visibleEmployees = computed(() => filterEmployees(this.employees(), this.filters()));

  /**
   * Cardurile de sumar arată totalul absolut pentru data curentă (astăzi),
   * ignorând complet ziua selectată sau navigația din calendar și filtrele de UI.
   */
  readonly summary = computed<AppointmentSummary>(() => {
    // Folosim strict data de astăzi în timp real
    const today = new Date();
    
    // Filtrăm doar programările care au data de start în ziua de azi
    const items = this.appointments().filter((a) => isSameDay(new Date(a.start), today));
    
    const count = (s: string) => items.filter((a) => a.status === s).length;
    const confirmed = count('confirmed');
    const pending = count('pending');
    const cancelled = count('cancelled');
    
    return { 
      total: items.length, 
      confirmed, 
      pending, 
      cancelled 
    };
  });

  onViewChange(view: CalendarView): void {
    this.view.set(view);
    // Săptămâna arată un singur angajat, deci filtrul trebuie să spună care.
    if (view === 'week' && !this.filters().employeeId) {
      const first = this.visibleEmployees()[0];
      if (first) this.filters.update((f) => ({ ...f, employeeId: first.id }));
    }
  }

  openAddAppointment(): void {
    // TODO: deschide formularul de adaugare
  }
}