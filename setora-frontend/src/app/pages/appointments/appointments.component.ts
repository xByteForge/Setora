import { ChangeDetectionStrategy, Component, computed, DestroyRef, inject, signal } from '@angular/core';
import { TranslocoModule } from '@ngneat/transloco';
import { AppointmentsHeaderComponent } from './appointment-header/appointment-header.component';
import { AppointmentFiltersComponent } from './appointment-filters/appointment-filters.component';
import { AppointmentCalendarComponent } from './appointment-calendar/appointment-calendar.component';
import { AppointmentSummary } from '../../core/ui-models/appointment-summary.model';
import { AppointmentFilters, DEFAULT_FILTERS } from '../../core/ui-models/appointment-filters.model';
import { CalendarView } from '../../core/ui-models/calendar-view.type';
import { CalendarService } from '../../core/services/calendar.service';
import { CatalogService } from '../../core/services/catalog.service';
import { AppointmentDetailsComponent } from './appointment-details/appointment-details.component';
import { AppointmentAgendaComponent } from './appointment-agenda/appointment-agenda.component';
import { AppointmentFormDialogComponent } from './appointment-form-dialog/appointment-form-dialog.component';
import { CancelAppointmentDialogComponent } from './cancel-appointment-dialog/cancel-appointment-dialog.component';
import { Appointment } from '../../core/models/appointment.model';
import { businessDate, todayIn } from '../../core/utils/date.util';
import { filterAppointments, filterEmployees } from './appointment-filters/appointment-filters.util';

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
  private readonly catalog = inject(CatalogService);

  // Pagina deține datele; calendarul doar le afișează.
  readonly business = signal(this.catalog.getBusiness());
  readonly employees = signal(this.calendarService.getEmployees());
  readonly appointments = computed(() => this.calendarService.getAppointments());

  /** Locația e a businessului, deci e aceeași pentru toate programările. */
  readonly locationLabel = computed(() => {
    const loc = this.business().location;
    return loc.locationName ?? loc.address ?? '';
  });

  readonly filters = signal<AppointmentFilters>(DEFAULT_FILTERS);
  /** Ziua afișată în calendar (sincronizată cu acesta). */
  readonly selectedDate = signal(new Date(2026, 10, 7));
  readonly view = signal<CalendarView>('day');

  // Se recalculează automat la orice schimbare de filtru.
  readonly visibleAppointments = computed(() =>
    filterAppointments(this.appointments(), this.filters(), this.business().timezone),
  );
  readonly visibleEmployees = computed(() => filterEmployees(this.employees(), this.filters()));


  /**
   * Cardurile de sumar arată totalul pentru azi (în fusul businessului),
   * ignorând ziua selectată în calendar și filtrele.
   */
  readonly summary = computed<AppointmentSummary>(() => {
    const tz = this.business().timezone;
    const today = todayIn(tz);
    const items = this.appointments().filter((a) => businessDate(a.start, tz) === today);

    return {
      total: items.length,
      confirmed: items.filter((a) => a.status === 'CONFIRMED').length,
      cancelled: items.filter((a) => a.status === 'CANCELLED').length,
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

  onAppointmentCreated(created: Appointment): void {
    this.addOpen.set(false);
    const [y, m, d] = businessDate(created.start, this.business().timezone).split('-').map(Number);
    this.selectedDate.set(new Date(y, m - 1, d));
    this.selectedId.set(created.id);
    this.showToast('APPOINTMENTS.FORM.SUCCESS');
  }

  // Selecția și detaliile
  readonly selectedId = signal<number | null>(null);

  /** Null when nothing is selected, or when filters hide the selected item. */
  readonly selectedAppointment = computed<Appointment | null>(() => {
    const id = this.selectedId();
    return id === null ? null : (this.visibleAppointments().find((a) => a.id === id) ?? null);
  });

  closeDetails(): void {
    this.selectedId.set(null);
  }

  // Anulare
  readonly cancelingId = signal<number | null>(null);
  readonly cancelTarget = computed<Appointment | null>(() => {
    const id = this.cancelingId();
    return id === null ? null : (this.appointments().find((a) => a.id === id) ?? null);
  });
  readonly cancelBusy = signal(false);
  readonly cancelFailed = signal(false);

  cancelAppointment(id: number): void {
    this.cancelFailed.set(false);
    this.cancelingId.set(id);
  }

  closeCancel(): void {
    if (!this.cancelBusy()) this.cancelingId.set(null);
  }

  confirmCancel(): void {
    const id = this.cancelingId();
    if (id === null) return;

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