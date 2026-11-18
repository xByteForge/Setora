import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { Appointment } from '../models/appointment.model';
import { Employee } from '../models/employee.model';
import { CreateAppointmentPayload } from '../ui-models/create-appointment-payload.model';
import { AppointmentSummary } from '../ui-models/appointment-summary.model';
import { addMinutes, businessLocalToInstant, toIsoDate } from '../utils/date.util';
import { CatalogService } from './catalog.service';

const WORK_START = 9 * 60; // 09:00
const WORK_END = 18 * 60;  // 18:00
const SLOT_STEP = 30;      // din 30 în 30 de minute

const toHHmm = (min: number): string =>
    `${String(Math.floor(min / 60)).padStart(2, '0')}:${String(min % 60).padStart(2, '0')}`;

const INITIAL_EMPLOYEES: Employee[] = [
    { id: 1, businessId: 1, userId: 1, active: true, serviceIds: [1, 2], name: 'Popescu Maria', employmentRole: 'Specialist' },
    { id: 2, businessId: 1, userId: 2, active: true, serviceIds: [2, 3], name: 'Ionescu Andrei', employmentRole: 'Specialist' },
];

export interface BackendAppointmentDto {
    id: number;
    businessId: number;
    serviceId: number;
    serviceName: string;
    durationMinutes: number;
    clientId: number;
    clientName: string;
    clientPhone: string | null;
    clientEmail: string | null;
    employeeId: number;
    employeeName: string;
    date: string;
    startTime: string;
    endTime: string;
    status: any;
    price: number;
    paymentMethod: any;
    clientNote: string | null;
}

export interface BackendSummaryDto {
    number: number;
    confirmed: number;
    pending?: number;
    cancelled: number;
}

@Injectable({ providedIn: 'root' })
export class CalendarService {
    private readonly http = inject(HttpClient);
    private readonly catalog = inject(CatalogService);
    private readonly apiUrl = '/api/appointments';

    private readonly appointmentsSignal = signal<Appointment[]>([]);
    private readonly employeesSignal = signal<Employee[]>(INITIAL_EMPLOYEES);

    constructor() {
        this.loadAppointments();
    }

    /** Încarcă programările din backend pentru business-ul dat */
    loadAppointments(businessId: number = 1): void {
        const params = new HttpParams().set('businessId', businessId);
        const tz = this.timezone();

        this.http.get<BackendAppointmentDto[]>(this.apiUrl, { params }).subscribe({
            next: (data) => {
                const mapped: Appointment[] = data.map((item) => {
                    const dateStr = item.date;
                    const startTimeStr = item.startTime?.length >= 5 ? item.startTime.substring(0, 5) : '00:00';
                    const endTimeStr = item.endTime?.length >= 5 ? item.endTime.substring(0, 5) : '00:00';

                    const start = businessLocalToInstant(dateStr, startTimeStr, tz);
                    const end = businessLocalToInstant(dateStr, endTimeStr, tz);

                    return {
                        id: item.id,
                        clientId: item.clientId ?? null,
                        clientName: item.clientName ?? '',
                        clientPhone: item.clientPhone ?? null,
                        clientEmail: item.clientEmail ?? null,
                        clientNote: item.clientNote ?? null,
                        employeeId: item.employeeId,
                        employeeName: item.employeeName ?? '',
                        serviceId: item.serviceId,
                        serviceName: item.serviceName ?? '',
                        start,
                        end,
                        status: item.status,
                        priceAtAppointment: Number(item.price ?? 0),
                        paymentMethod: item.paymentMethod ?? 'PHYSICAL',
                        createdAt: new Date().toISOString(),
                    };
                });

                this.appointmentsSignal.set(mapped);

                // Extragem dinamic angajații unici din programări și îi îmbinăm cu lista inițială
                const employeeMap = new Map<number, Employee>();
                INITIAL_EMPLOYEES.forEach((emp) => employeeMap.set(emp.id, { ...emp }));

                data.forEach((item) => {
                    if (item.employeeId && item.employeeName) {
                        const existing = employeeMap.get(item.employeeId);
                        if (existing) {
                            if (item.serviceId && !existing.serviceIds.includes(item.serviceId)) {
                                existing.serviceIds.push(item.serviceId);
                            }
                        } else {
                            employeeMap.set(item.employeeId, {
                                id: item.employeeId,
                                businessId: item.businessId || businessId,
                                userId: item.employeeId,
                                active: true,
                                serviceIds: item.serviceId ? [item.serviceId] : [],
                                name: item.employeeName,
                                employmentRole: 'Specialist',
                            });
                        }
                    }
                });

                this.employeesSignal.set(Array.from(employeeMap.values()));
            },
            error: (err) => console.error('Eroare la încărcarea programărilor:', err),
        });
    }

    getAppointments(): Appointment[] {
        return this.appointmentsSignal();
    }

    getEmployees(): Employee[] {
        return this.employeesSignal();
    }

    /** Obține sumarul zilnic corespunzător rutei GET /api/appointments/summary */
    getSummary(businessId: number, date: string): Observable<AppointmentSummary> {
        const params = new HttpParams()
            .set('businessId', businessId)
            .set('date', date);

        return this.http.get<BackendSummaryDto>(`${this.apiUrl}/summary`, { params }).pipe(
            map((res) => ({
                total: res.number ?? 0,
                confirmed: res.confirmed ?? 0,
                cancelled: res.cancelled ?? 0,
            }))
        );
    }

    /** Calculează sloturile libere pe baza programărilor existente */
    getAvailableSlots(employeeId: number | null, date: string, durationMinutes: number): string[] {
        const slots: string[] = [];
        const currentAppointments = this.appointmentsSignal();
        const tz = this.timezone();

        const dayAppointments = currentAppointments.filter((app) => {
            if (!app.start) return false;
            const matchesEmployee = employeeId === null || app.employeeId === employeeId;
            return matchesEmployee && app.status !== 'CANCELLED';
        });

        for (let m = WORK_START; m + durationMinutes <= WORK_END; m += SLOT_STEP) {
            const time = toHHmm(m);
            const slotStart = businessLocalToInstant(date, time, tz);
            const slotEnd = addMinutes(slotStart, durationMinutes);
            const startMs = Date.parse(slotStart);
            const endMs = Date.parse(slotEnd);

            const isOverlap = dayAppointments.some((app) => {
                const aStartMs = Date.parse(app.start);
                const aEndMs = Date.parse(app.end);
                return startMs < aEndMs && endMs > aStartMs;
            });

            if (!isOverlap) {
                slots.push(time);
            }
        }

        return slots;
    }

    /** Creează o programare prin POST /api/appointments/ghost */
    create(payload: CreateAppointmentPayload): Observable<Appointment> {
        const chosenEmployeeId = payload.employeeId ? Number(payload.employeeId) : 1;
        const timeStr = payload.startTime?.length === 5 ? `${payload.startTime}:00` : payload.startTime;

        const backendPayload = {
            businessId: 1,
            serviceId: payload.serviceId,
            employeeId: chosenEmployeeId,
            clientName: payload.clientName,
            clientPhone: payload.clientPhone,
            clientEmail: payload.clientEmail || 'client1@setora.test',
            date: payload.date,
            startTime: timeStr,
            paymentMethod: 'PHYSICAL',
            clientNote: payload.clientNote || null,
        };

        const tz = this.timezone();

        return this.http.post<BackendAppointmentDto>(`${this.apiUrl}/ghost`, backendPayload).pipe(
            map((created) => {
                const dateStr = created.date || payload.date;
                const sTime = created.startTime?.length >= 5 ? created.startTime.substring(0, 5) : payload.startTime;
                const eTime = created.endTime?.length >= 5 ? created.endTime.substring(0, 5) : sTime;

                const normalized: Appointment = {
                    id: created.id,
                    clientId: created.clientId ?? null,
                    clientName: created.clientName ?? payload.clientName,
                    clientPhone: created.clientPhone ?? payload.clientPhone,
                    clientEmail: created.clientEmail ?? payload.clientEmail,
                    clientNote: created.clientNote ?? payload.clientNote ?? null,
                    employeeId: created.employeeId ?? chosenEmployeeId,
                    employeeName: created.employeeName ?? '',
                    serviceId: created.serviceId ?? payload.serviceId,
                    serviceName: created.serviceName ?? '',
                    start: businessLocalToInstant(dateStr, sTime, tz),
                    end: businessLocalToInstant(dateStr, eTime, tz),
                    status: created.status ?? 'CONFIRMED',
                    priceAtAppointment: Number(created.price ?? 0),
                    paymentMethod: created.paymentMethod ?? 'PHYSICAL',
                    createdAt: new Date().toISOString(),
                };

                this.appointmentsSignal.update((list) => [...list, normalized]);
                return normalized;
            })
        );
    }

    /** Anulează o programare prin PATCH /api/appointments/{id}/cancel */
    cancel(id: number): Observable<Appointment> {
        const tz = this.timezone();

        return this.http.patch<BackendAppointmentDto>(`${this.apiUrl}/${id}/cancel`, {}).pipe(
            map((cancelled) => {
                const current = this.appointmentsSignal().find((a) => a.id === id);
                const dateStr = cancelled.date || '2026-11-23';
                const sTime = cancelled.startTime?.length >= 5 ? cancelled.startTime.substring(0, 5) : '00:00';
                const eTime = cancelled.endTime?.length >= 5 ? cancelled.endTime.substring(0, 5) : '00:00';

                const normalized: Appointment = {
                    id: cancelled.id,
                    clientId: cancelled.clientId ?? current?.clientId ?? null,
                    clientName: cancelled.clientName ?? current?.clientName ?? '',
                    clientPhone: cancelled.clientPhone ?? current?.clientPhone ?? null,
                    clientEmail: cancelled.clientEmail ?? current?.clientEmail ?? null,
                    clientNote: cancelled.clientNote ?? current?.clientNote ?? null,
                    employeeId: cancelled.employeeId ?? current?.employeeId ?? 1,
                    employeeName: cancelled.employeeName ?? current?.employeeName ?? '',
                    serviceId: cancelled.serviceId ?? current?.serviceId ?? 1,
                    serviceName: cancelled.serviceName ?? current?.serviceName ?? '',
                    start: businessLocalToInstant(dateStr, sTime, tz),
                    end: businessLocalToInstant(dateStr, eTime, tz),
                    status: cancelled.status ?? 'CANCELLED',
                    priceAtAppointment: Number(cancelled.price ?? current?.priceAtAppointment ?? 0),
                    paymentMethod: cancelled.paymentMethod ?? current?.paymentMethod ?? 'PHYSICAL',
                    createdAt: current?.createdAt || new Date().toISOString(),
                };

                this.appointmentsSignal.update((list) => list.map((a) => (a.id === id ? normalized : a)));
                return normalized;
            })
        );
    }

    private timezone(): string {
        return this.catalog.getBusiness().timezone;
    }
}
