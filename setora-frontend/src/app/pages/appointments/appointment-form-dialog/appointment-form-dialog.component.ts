import { ChangeDetectionStrategy, Component, computed, inject, input, OnInit, output, signal } from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { TranslocoModule } from '@ngneat/transloco';
import { CalendarService } from '../../../core/services/calendar.service';
import { CatalogService } from '../../../core/services/catalog.service';
import { CalendarAppointment } from '../../../core/models/calendar-appointment.model';
import { Location } from '../../../core/models/location.model';
import { HostListener } from '@angular/core';

const PHONE_PATTERN = /^(\+40|0040|0)7\d{2}[\s.-]?\d{3}[\s.-]?\d{3}$/;



const toIsoDate = (d: Date) =>
    `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;

const addMinutes = (hhmm: string, minutes: number) => {
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
    readonly locations = input.required<Location[]>();
    readonly closed = output<void>();
    readonly created = output<CalendarAppointment>();

    readonly services = this.catalog.getServices();
    private readonly allEmployees = this.calendar.getEmployees();

    readonly form = this.fb.group({
        clientName: ['', [Validators.required, Validators.minLength(2)]],
        clientPhone: ['', [Validators.required, Validators.pattern(PHONE_PATTERN)]],
        clientEmail: ['', [Validators.email]],
        serviceId: ['', Validators.required],
        locationId: ['', Validators.required],
        employeeId: [''], // '' = orice angajat disponibil
        date: ['', Validators.required],
        startTime: ['', Validators.required],
        clientNote: ['', Validators.maxLength(500)],
    });

    readonly submitting = signal(false);
    readonly error = signal(false);

    private readonly value = toSignal(this.form.valueChanges, { initialValue: this.form.getRawValue() });

    readonly employeesForLocation = computed(() =>
        this.allEmployees.filter((e) => e.locationIds.includes(this.value().locationId ?? '')),
    );

    readonly selectedService = computed(() => this.services.find((s) => s.id === this.value().serviceId));

    readonly slots = computed(() => {
        const v = this.value();
        const service = this.selectedService();
        if (!service || !v.locationId || !v.date) return [];
        return this.calendar.getAvailableSlots(v.locationId, v.employeeId || null, v.date, service.durationMinutes);
    });

    readonly endTime = computed(() => {
        const start = this.value().startTime;
        const service = this.selectedService();
        return start && service ? addMinutes(start, service.durationMinutes) : '';
    });

    constructor() {
        const c = this.form.controls;

        // Schimbarea locatiei reseteaza angajatul (poate sa nu lucreze acolo)
        c.locationId.valueChanges.pipe(takeUntilDestroyed()).subscribe(() => c.employeeId.setValue(''));

        // Orice schimbare care modifica orele libere reseteaza ora aleasa
        [c.serviceId, c.locationId, c.employeeId, c.date].forEach((ctrl) =>
            ctrl.valueChanges.pipe(takeUntilDestroyed()).subscribe(() => c.startTime.setValue('')),
        );
    }

    ngOnInit(): void {
        this.form.patchValue({
            date: toIsoDate(this.selectedDate()),
            locationId: this.locations()[0]?.id ?? '',
            serviceId: this.services[0]?.id ?? '',
        });
    }

    submit(): void {
        if (this.form.invalid) {
            this.form.markAllAsTouched();
            return;
        }
        const service = this.selectedService()!;
        const v = this.form.getRawValue();

        this.submitting.set(true);
        this.error.set(false);

        this.calendar
            .create(
                {
                    clientName: v.clientName.trim(),
                    clientPhone: v.clientPhone,
                    clientEmail: v.clientEmail,
                    serviceId: v.serviceId,
                    locationId: v.locationId,
                    employeeId: v.employeeId || null,
                    date: v.date,
                    startTime: v.startTime,
                    clientNote: v.clientNote.trim() || undefined,
                },
                service,
            )
            .subscribe({
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