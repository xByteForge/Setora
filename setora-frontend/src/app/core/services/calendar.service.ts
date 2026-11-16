import { Injectable, inject, signal } from '@angular/core';
import { Observable, map, timer } from 'rxjs';
import { Appointment } from '../models/appointment.model';

import { Employee } from '../models/employee.model';
import { Service } from '../models/service.model';
import { CreateAppointmentPayload } from '../ui-models/create-appointment-payload.model';
import { addMinutes, businessLocalToInstant } from '../utils/date.util';
import { CatalogService } from './catalog.service';
import { AppointmentStatus } from '../models/enums/appointment-status.model';

const WORK_START = 9 * 60;
const WORK_END = 18 * 60;
const SLOT_STEP = 30;
const DAY = '2026-11-07';

/** Pauza de masa: backendul cere un serviciu pentru fiecare programare, deci folosim unul placeholder. */
const BREAK_SERVICE: Service = {
    id: 0,
    businessId: 1,
    name: 'Pauză de masă',
    description: null,
    price: 0,
    durationMinutes: 60,
    active: false,
    paymentMethod: 'PHYSICAL',
};

/** Date mock. serviceId 0 = pauza. */
interface SeedRow {
    id: number;
    employeeId: number;
    serviceId: number;
    clientName: string;
    date: string;
    time: string;
    status: AppointmentStatus;
    phone?: string;
    email?: string;
    note?: string;
}

const SEED_ROWS: SeedRow[] = [
    // Ana
    { id: 1, employeeId: 1, serviceId: 1, clientName: 'Irina Popescu', date: DAY, time: '09:30', status: 'CONFIRMED', phone: '0700 000 123', email: 'irina@example.com', note: 'Aș dori să păstrez lungimea.' },
    { id: 2, employeeId: 1, serviceId: 1, clientName: 'Elena Dobre', date: DAY, time: '11:00', status: 'CONFIRMED' },
    { id: 3, employeeId: 1, serviceId: 2, clientName: 'Maria Șerban', date: DAY, time: '13:00', status: 'CONFIRMED' },
    // Mihai
    { id: 4, employeeId: 2, serviceId: 3, clientName: 'Andrei Pavel', date: DAY, time: '09:00', status: 'CONFIRMED' },
    { id: 6, employeeId: 2, serviceId: 0, clientName: 'Pauză de masă', date: DAY, time: '12:00', status: 'UNAVAILABLE' },
    { id: 7, employeeId: 2, serviceId: 3, clientName: 'Vlad Marinescu', date: DAY, time: '14:00', status: 'CONFIRMED' },
    // Ioana
    { id: 8, employeeId: 3, serviceId: 4, clientName: 'Diana Enache', date: DAY, time: '10:00', status: 'CONFIRMED' },
    { id: 9, employeeId: 3, serviceId: 4, clientName: 'Alina Tudor', date: DAY, time: '11:30', status: 'CANCELLED' },
    { id: 10, employeeId: 3, serviceId: 4, clientName: 'Oana Dumitru', date: DAY, time: '14:30', status: 'CONFIRMED' },
    // Alte zile
    { id: 11, employeeId: 1, serviceId: 1, clientName: 'Laura Matei', date: '2026-11-02', time: '10:00', status: 'CONFIRMED' },
    { id: 13, employeeId: 3, serviceId: 4, clientName: 'Sara Popa', date: '2026-11-05', time: '13:30', status: 'CANCELLED' },
];

interface AppointmentInit {
    id: number;
    employee: Employee;
    service: Service;
    startsAt: string;
    clientName: string;
    clientPhone: string | null;
    clientEmail: string | null;
    clientNote: string | null;
    status: AppointmentStatus;
}

/** Construieste un Appointment (forma DTO) din datele unei programari si ale serviciului. */
const buildAppointment = (i: AppointmentInit): Appointment => ({
    id: i.id,
    clientId: null, // ghost client: datele sunt in campurile de client
    clientName: i.clientName,
    clientPhone: i.clientPhone,
    clientEmail: i.clientEmail,
    clientNote: i.clientNote,
    employeeId: i.employee.id,
    employeeName: i.employee.name,
    serviceId: i.service.id,
    serviceName: i.service.name,
    start: i.startsAt,
    end: addMinutes(i.startsAt, i.service.durationMinutes),
    status: i.status,
    priceAtAppointment: i.service.price,
    paymentMethod: i.service.paymentMethod,
    createdAt: new Date().toISOString(),
});

const toHHmm = (min: number): string =>
    `${String(Math.floor(min / 60)).padStart(2, '0')}:${String(min % 60).padStart(2, '0')}`;

@Injectable({ providedIn: 'root' })
export class CalendarService {
    private readonly catalog = inject(CatalogService);

    private readonly employees: Employee[] = [
        { id: 1, businessId: 1, userId: 11, active: true, serviceIds: [1, 2], name: 'Ana Ionescu', employmentRole: 'Coafură' },
        { id: 2, businessId: 1, userId: 12, active: true, serviceIds: [3], name: 'Mihai Radu', employmentRole: 'Frizerie' },
        { id: 3, businessId: 1, userId: 13, active: true, serviceIds: [4], name: 'Ioana Matei', employmentRole: 'Îngrijire' },
    ];

    private nextId = 1000;

    private readonly appointments = signal<Appointment[]>(this.seed());

    getEmployees(): Employee[] {
        return this.employees;
    }

    getAppointments(): Appointment[] {
        return this.appointments();
    }

    /** Orele libere (HH:mm, in fusul businessului) pentru un serviciu, intr-o zi. */
    getAvailableSlots(employeeId: number | null, date: string, durationMinutes: number): string[] {
        const tz = this.timezone();
        const candidates = this.employees.filter(
            (e) => e.active && (employeeId === null || e.id === employeeId),
        );

        const slots: string[] = [];
        for (let m = WORK_START; m + durationMinutes <= WORK_END; m += SLOT_STEP) {
            const time = toHHmm(m);
            const startsAt = businessLocalToInstant(date, time, tz);
            const endsAt = addMinutes(startsAt, durationMinutes);
            if (candidates.some((e) => this.isEmployeeFree(e.id, startsAt, endsAt))) {
                slots.push(time);
            }
        }
        return slots;
    }

    create(payload: CreateAppointmentPayload): Observable<Appointment> {
        return timer(500).pipe( // simuleaza intarzierea unui API
            map(() => {
                const service = this.catalog.getServices().find((s) => s.id === payload.serviceId);
                if (!service) throw new Error('NOT_FOUND');

                const startsAt = businessLocalToInstant(payload.date, payload.startTime, this.timezone());
                const endsAt = addMinutes(startsAt, service.durationMinutes);

                // primul angajat liber (sau cel ales)
                const employee = this.employees
                    .filter((e) => e.active && (payload.employeeId === null || e.id === payload.employeeId))
                    .find((e) => this.isEmployeeFree(e.id, startsAt, endsAt));
                if (!employee) throw new Error('SLOT_TAKEN');

                const created = buildAppointment({
                    id: this.nextId++,
                    employee,
                    service,
                    startsAt,
                    clientName: payload.clientName,
                    clientPhone: payload.clientPhone,
                    clientEmail: payload.clientEmail || null,
                    clientNote: payload.clientNote ?? null,
                    status: 'CONFIRMED',
                });

                this.appointments.update((list) => [...list, created]);
                return created;
            }),
        );
    }

    /** Marcheaza programarea ca anulata. Intervalul devine liber (isEmployeeFree ignora anulatele). */
    cancel(id: number): Observable<Appointment> {
        return timer(500).pipe(
            map(() => {
                const current = this.findOrThrow(id);
                const cancelled: Appointment = { ...current, status: 'CANCELLED' };
                this.appointments.update((list) => list.map((a) => (a.id === id ? cancelled : a)));
                return cancelled;
            }),
        );
    }

    private seed(): Appointment[] {
        const tz = this.timezone();
        return SEED_ROWS.map((r) => {
            const employee = this.employees.find((e) => e.id === r.employeeId)!;
            const service = this.catalog.getServices().find((s) => s.id === r.serviceId) ?? BREAK_SERVICE;
            return buildAppointment({
                id: r.id,
                employee,
                service,
                startsAt: businessLocalToInstant(r.date, r.time, tz),
                clientName: r.clientName,
                clientPhone: r.phone ?? null,
                clientEmail: r.email ?? null,
                clientNote: r.note ?? null,
                status: r.status,
            });
        });
    }

    /** Un angajat e liber daca nu are o programare non-anulata care se suprapune. */
    private isEmployeeFree(employeeId: number, start: string, end: string): boolean {
        const from = Date.parse(start);
        const to = Date.parse(end);
        return !this.appointments().some(
            (a) =>
                a.employeeId === employeeId &&
                a.status !== 'CANCELLED' &&
                Date.parse(a.start) < to &&
                Date.parse(a.end) > from,
        );
    }

    private findOrThrow(id: number): Appointment {
        const found = this.appointments().find((a) => a.id === id);
        if (!found) throw new Error('NOT_FOUND');
        return found;
    }

    private timezone(): string {
        return this.catalog.getBusiness().timezone;
    }
}