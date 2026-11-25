import {
  ChangeDetectionStrategy,
  Component,
  computed,
  HostListener,
  inject,
  output,
  signal,
  OnInit,
} from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { TranslocoModule } from '@ngneat/transloco';
import { map } from 'rxjs';
import { CalendarService } from '../../../core/services/calendar.service';
import { CatalogService, CURRENCY } from '../../../core/services/catalog.service';
import { Appointment } from '../../../core/models/appointment.model';
import { Employee } from '../../../core/models/employee.model';
import { Service } from '../../../core/models/service.model';
import { CreateAppointmentPayload } from '../../../core/ui-models/create-appointment-payload.model';
import { toIsoDate } from '../../../core/utils/date.util';
import { input } from '@angular/core';

const PHONE_PATTERN = /^(\+40|0040|0)7\d{2}[\s.-]?\d{3}[\s.-]?\d{3}$/;

/** Adauga minute la un HH:mm. Nu are nevoie de fus, lucreaza doar cu ora afisata. */
const addHhmm = (hhmm: string, minutes: number): string => {
  const total = +hhmm.slice(0, 2) * 60 + +hhmm.slice(3, 5) + minutes;
  return `${String(Math.floor(total / 60)).padStart(2, '0')}:${String(total % 60).padStart(2, '0')}`;
};

@Component({
  selector: 'app-appointment-form-dialog',
  imports: [ReactiveFormsModule, TranslocoModule],
  templateUrl: './appointment-form-dialog.component.html',
  styleUrl: './appointment-form-dialog.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AppointmentFormDialogComponent implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly calendar = inject(CalendarService);
  private readonly catalog = inject(CatalogService);

  readonly selectedDate = input.required<Date>();
  readonly closed = output<void>();
  readonly created = output<Appointment>();

  readonly currency = CURRENCY;
  readonly services: Service[] = this.catalog.getServices().filter((s) => s.active);
  private readonly activeEmployees: Employee[] = this.calendar
    .getEmployees()
    .filter((e) => e.active);

  // Controalele de id sunt string-uri, pentru ca <select> trimite string-uri.
  readonly form = this.fb.group({
    clientName: ['', [Validators.required, Validators.minLength(2)]],
    clientPhone: ['', [Validators.required, Validators.pattern(PHONE_PATTERN)]],
    clientEmail: ['', [Validators.email]],
    serviceId: ['', Validators.required],
    employeeId: [''], // '' = orice angajat disponibil
    date: ['', Validators.required],
    startTime: ['', Validators.required],
    clientNote: ['', Validators.maxLength(500)],
  });

  readonly submitting = signal(false);
  readonly error = signal(false);

  private readonly value = toSignal(
    this.form.valueChanges.pipe(map(() => this.form.getRawValue())),
    { initialValue: this.form.getRawValue() },
  );

  readonly selectedService = computed<Service | undefined>(() =>
    this.services.find((s) => String(s.id) === this.value().serviceId),
  );

  /** Doar angajatii care pot face serviciul ales. */
  readonly employeesForService = computed<Employee[]>(() => {
    const service = this.selectedService();
    return service ? this.activeEmployees.filter((e) => e.serviceIds.includes(service.id)) : [];
  });

  readonly slots = computed(() => {
    const v = this.value();
    const service = this.selectedService();
    if (!service || !v.date) return [];
    return this.calendar.getAvailableSlots(
      v.employeeId ? Number(v.employeeId) : null,
      v.date,
      service.durationMinutes,
    );
  });

  readonly endTime = computed(() => {
    const start = this.value().startTime;
    const service = this.selectedService();
    return start && service ? addHhmm(start, service.durationMinutes) : '';
  });

  constructor() {
    const c = this.form.controls;

    // Schimbarea serviciului reseteaza angajatul (poate sa nu-l faca)
    c.serviceId.valueChanges.pipe(takeUntilDestroyed()).subscribe(() => c.employeeId.setValue(''));

    // Orice schimbare care modifica orele libere reseteaza ora aleasa
    [c.serviceId, c.employeeId, c.date].forEach((ctrl) =>
      ctrl.valueChanges.pipe(takeUntilDestroyed()).subscribe(() => c.startTime.setValue('')),
    );
  }

  ngOnInit(): void {
    this.form.patchValue({
      date: toIsoDate(this.selectedDate()),
      serviceId: this.services[0] ? String(this.services[0].id) : '',
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    this.error.set(false);

    const v = this.form.getRawValue();
    const payload: CreateAppointmentPayload = {
      clientName: v.clientName.trim(),
      clientPhone: v.clientPhone,
      clientEmail: v.clientEmail,
      serviceId: Number(v.serviceId),
      employeeId: v.employeeId ? Number(v.employeeId) : null,
      date: v.date,
      startTime: v.startTime,
      clientNote: v.clientNote.trim() || undefined,
    };

    this.calendar.create(payload).subscribe({
      next: (appointment) => {
        this.submitting.set(false);
        this.created.emit(appointment);
      },
      error: () => {
        this.submitting.set(false);
        this.error.set(true);
      },
    });
  }

  close(): void {
    this.closed.emit();
  }

  /** True daca dupa ce a fost atins, campul are eroarea data. */
  hasError(name: string, error: string): boolean {
    const ctrl = this.form.get(name);
    return !!ctrl && ctrl.touched && ctrl.hasError(error);
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    this.close();
  }
}
