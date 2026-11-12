import { ChangeDetectionStrategy, Component, computed, DestroyRef, inject, signal } from '@angular/core';
import { TranslocoModule } from '@ngneat/transloco';
import { AppointmentsHeaderComponent } from './appointment-header/appointment-header.component';
import { AppointmentFiltersComponent } from './appointment-filters/appointment-filters.component';
import { AppointmentCalendarComponent } from './appointment-calendar/appointment-calendar.component';
import { AppointmentSummary } from '../../core/models/appointment-summary.model';
import { AppointmentFilters, DEFAULT_FILTERS } from '../../core/models/appointment-filters.model';
import { CalendarView } from '../../core/models/calendar-view.type';
import { CalendarService } from '../../core/services/calendar.service';
import { filterAppointments, filterEmployees, isSameDay } from './appointment-filters/appointment-filters.util';
import { AppointmentDetailsComponent } from './appointment-details/appointment-details.component';
import { AppointmentAgendaComponent } from './appointment-agenda/appointment-agenda.component';
import { CalendarAppointment } from '../../core/models/calendar-appointment.model';
import { CatalogService } from '../../core/services/catalog.service';
import { AppointmentFormDialogComponent } from './appointment-form-dialog/appointment-form-dialog.component';
import { CancelAppointmentDialogComponent } from './cancel-appointment-dialog/cancel-appointment-dialog.component';

@Component({
  imports: [
    TranslocoModule,
    AppointmentsHeaderComponent,
    AppointmentCalendarComponent,
    AppointmentFiltersComponent,
    AppointmentDetailsComponent,
    AppointmentAgendaComponent,
    AppointmentFormDialogComponent,
    CancelAppointmentDialogComponent,
  ],
  selector: 'app-appointments',
  templateUrl: './appointments.component.html',
  styleUrl: './appointments.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AppointmentsComponent {
  private readonly calendarService = inject(CalendarService);

  // Pagina deține datele; calendarul doar le afișează.
  private readonly catalog = inject(CatalogService);
  readonly locations = signal(this.catalog.getLocations());
  readonly employees = signal(this.calendarService.getEmployees());
  readonly appointments = computed(() => this.calendarService.getAppointments());

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
      cancelled,
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

  // Starea dialogului de adăugare
  readonly addOpen = signal(false);

  openAddAppointment(): void {
    this.addOpen.set(true);
  }

  closeAddAppointment(): void {
    this.addOpen.set(false);
  }

  readonly toast = signal<string | null>(null);

  onAppointmentCreated(created: CalendarAppointment): void {
    this.addOpen.set(false);
    const [y, m, d] = created.start.slice(0, 10).split('-').map(Number);
    this.selectedDate.set(new Date(y, m - 1, d));
    this.selectedId.set(created.id);
    this.showToast('APPOINTMENTS.FORM.SUCCESS');
  }

  readonly selectedId = signal<string | null>(null);

  /** Null when nothing is selected, or when filters hide the selected item. */
  readonly selectedAppointment = computed(() => {
    const id = this.selectedId();
    const item = id ? this.visibleAppointments().find((a) => a.id === id) : undefined;
    return item ? this.calendarService.getDetails(item) : null;
  });

  readonly selectedLocationName = computed(() => {
    const id = this.appointments().find((a) => a.id === this.selectedId())?.locationId;
    return this.locations().find((l) => l.id === id)?.name ?? '';
  });

  closeDetails(): void {
    this.selectedId.set(null);
  }

  // Editare
  readonly editingId = signal<string | null>(null);
  readonly editTarget = computed(() => {
    const id = this.editingId();
    const item = id ? this.appointments().find((a) => a.id === id) : undefined;
    return item ? this.calendarService.getDetails(item) : null;
  });

  editAppointment(id: string): void {
    this.editingId.set(id);
  }

  closeEdit(): void {
    this.editingId.set(null);
  }

  onAppointmentUpdated(updated: CalendarAppointment): void {
    this.editingId.set(null);
    this.selectedId.set(updated.id);
    this.showToast('APPOINTMENTS.FORM.UPDATED');
  }

  // Anulare
  readonly cancelingId = signal<string | null>(null);
  readonly cancelTarget = computed(() => {
    const id = this.cancelingId();
    const item = id ? this.appointments().find((a) => a.id === id) : undefined;
    return item ? this.calendarService.getDetails(item) : null;
  });
  readonly cancelBusy = signal(false);
  readonly cancelFailed = signal(false);

  cancelAppointment(id: string): void {
    this.cancelFailed.set(false);
    this.cancelingId.set(id);
  }

  closeCancel(): void {
    if (!this.cancelBusy()) this.cancelingId.set(null);
  }

  confirmCancel(): void {
    const id = this.cancelingId();
    if (!id) return;

    this.cancelBusy.set(true);
    this.cancelFailed.set(false);

    this.calendarService.cancel(id).subscribe({
      next: () => {
        this.cancelBusy.set(false);
        this.cancelingId.set(null);
        // selectedId rămâne setat, deci panoul arată statusul anulat
        this.showToast('APPOINTMENTS.CANCEL.SUCCESS');
      },
      error: () => {
        this.cancelBusy.set(false);
        this.cancelFailed.set(true);
      },
    });
  }

  locationName(locationId: string): string {
    return this.locations().find((l) => l.id === locationId)?.name ?? '';
  }

  private showToast(key: string): void {
    this.toast.set(key);
    setTimeout(() => this.toast.set(null), 4000);
  }

  private readonly mql = window.matchMedia('(max-width: 900px)');
  readonly isMobile = signal(this.mql.matches);

  constructor() {
    const onChange = (e: MediaQueryListEvent) => this.isMobile.set(e.matches);
    this.mql.addEventListener('change', onChange);
    inject(DestroyRef).onDestroy(() => this.mql.removeEventListener('change', onChange));
  }
}