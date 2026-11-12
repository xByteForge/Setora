import { Injectable, signal } from '@angular/core';
import { Observable, timer, map } from 'rxjs';
import { Employee } from '../models/employee.model';
import { CalendarAppointment } from '../models/calendar-appointment.model';
import { Appointment } from '../models/appointment.model';
import { Service } from '../models/service.model';                                   // NOU
import { CreateAppointmentPayload } from '../models/create-appointment-payload.model'; // NOU


const DAY = '2026-11-07';
const at = (time: string) => `${DAY}T${time}:00`;
const CENTRU = 'buc-centru';
const NORD = 'buc-nord';

// "09:30" -> 570 si invers
const toMinutes = (hhmm: string) => +hhmm.slice(0, 2) * 60 + +hhmm.slice(3, 5);
const toHHmm = (min: number) =>
    `${String(Math.floor(min / 60)).padStart(2, '0')}:${String(min % 60).padStart(2, '0')}`;


const WORK_START = 9 * 60;
const WORK_END = 18 * 60;
const SLOT_STEP = 30;

@Injectable({ providedIn: 'root' })
export class CalendarService {
    private readonly employees: Employee[] = [
        { id: 'ana', name: 'Ana Ionescu', initials: 'AI', role: 'Coafură', locationIds: [CENTRU] },
        { id: 'mihai', name: 'Mihai Radu', initials: 'MR', role: 'Frizerie', locationIds: [CENTRU] },
        { id: 'ioana', name: 'Ioana Matei', initials: 'IM', role: 'Îngrijire', locationIds: [CENTRU, NORD] },
    ];

    private readonly appointments = signal<CalendarAppointment[]>([
        // Ana
        { id: '1', employeeId: 'ana', locationId: CENTRU, clientName: 'Irina Popescu', service: 'Tuns & coafat', start: at('09:30'), end: at('10:30'), status: 'confirmed' },
        { id: '2', employeeId: 'ana', locationId: CENTRU, clientName: 'Elena Dobre', service: 'Tuns & coafat', start: at('11:00'), end: at('12:00'), status: 'confirmed' },
        { id: '3', employeeId: 'ana', locationId: CENTRU, clientName: 'Maria Șerban', service: 'Vopsit & coafat', start: at('13:00'), end: at('14:30'), status: 'confirmed' },
        // Mihai
        { id: '4', employeeId: 'mihai', locationId: CENTRU, clientName: 'Andrei Pavel', service: 'Tuns & barbă', start: at('09:00'), end: at('10:00'), status: 'confirmed' },
        { id: '5', employeeId: 'mihai', locationId: CENTRU, clientName: 'Radu Stan', service: 'Tuns & barbă', start: at('10:30'), end: at('11:30'), status: 'pending' },
        { id: '6', employeeId: 'mihai', locationId: CENTRU, clientName: 'Pauză de masă', service: 'Interval blocat', start: at('12:00'), end: at('13:00'), status: 'unavailable' },
        { id: '7', employeeId: 'mihai', locationId: CENTRU, clientName: 'Vlad Marinescu', service: 'Tuns & barbă', start: at('14:00'), end: at('15:00'), status: 'confirmed' },
        // Ioana
        { id: '8', employeeId: 'ioana', locationId: CENTRU, clientName: 'Diana Enache', service: 'Manichiură', start: at('10:00'), end: at('11:00'), status: 'confirmed' },
        { id: '9', employeeId: 'ioana', locationId: CENTRU, clientName: 'Alina Tudor', service: 'Manichiură', start: at('11:30'), end: at('12:30'), status: 'cancelled' },
        { id: '10', employeeId: 'ioana', locationId: CENTRU, clientName: 'Oana Dumitru', service: 'Manichiură', start: at('14:30'), end: at('15:30'), status: 'confirmed' },

        { id: '11', employeeId: 'ana', locationId: CENTRU, clientName: 'Laura Matei', service: 'Tuns & coafat', start: '2026-11-02T10:00:00', end: '2026-11-02T11:00:00', status: 'confirmed' },
        { id: '12', employeeId: 'mihai', locationId: CENTRU, clientName: 'Paul Ionescu', service: 'Tuns & barbă', start: '2026-11-05T13:00:00', end: '2026-11-05T14:00:00', status: 'pending' },
        { id: '13', employeeId: 'ioana', locationId: NORD, clientName: 'Sara Popa', service: 'Manichiură', start: '2026-11-05T13:30:00', end: '2026-11-05T14:30:00', status: 'cancelled' },
    ]);

    private readonly extras: Record<string, Partial<Appointment>> = {
        '1': {
            clientPhone: '0700 000 123',
            clientEmail: 'irina@example.com',
            price: 150,
            paymentMethod: 'Physical',
            isPaid: false,
            clientNote: 'Aș dori să păstrez lungimea.',
        },
    };

    getEmployees(): Employee[] {
        return this.employees;
    }

    getAppointments(): CalendarAppointment[] {
        return this.appointments();
    }

    isEmployeeFree(employeeId: string, date: string, startTime: string, durationMinutes: number): boolean {
        const start = toMinutes(startTime);
        const end = start + durationMinutes;

        return !this.appointments().some((a) => {
            if (a.employeeId !== employeeId) return false;
            if (a.status === 'cancelled') return false; // anulatele nu blocheaza
            if (a.start.slice(0, 10) !== date) return false;

            const aStart = toMinutes(a.start.slice(11, 16));
            const aEnd = toMinutes(a.end.slice(11, 16));
            return start < aEnd && end > aStart; // suprapunere
        });
    }

    getAvailableSlots(locationId: string, employeeId: string | null, date: string, durationMinutes: number): string[] {
        const candidates = this.employees.filter(
            (e) => e.locationIds.includes(locationId) && (!employeeId || e.id === employeeId),
        );
        const slots: string[] = [];
        for (let m = WORK_START; m + durationMinutes <= WORK_END; m += SLOT_STEP) {
            const time = toHHmm(m);
            if (candidates.some((e) => this.isEmployeeFree(e.id, date, time, durationMinutes))) {
                slots.push(time);
            }
        }
        return slots;
    }

    create(payload: CreateAppointmentPayload, service: Service): Observable<CalendarAppointment> {
        return timer(500).pipe( // simuleaza intarzierea unui API
            map(() => {
                // angajatii din locatia aleasa (si doar cel ales, daca a ales unul)
                const candidates = this.employees.filter(
                    (e) =>
                        e.locationIds.includes(payload.locationId) &&
                        (payload.employeeId === null || e.id === payload.employeeId),
                );

                // primul care e liber la ora respectiva
                const employee = candidates.find((e) =>
                    this.isEmployeeFree(e.id, payload.date, payload.startTime, service.durationMinutes),
                );

                if (!employee) throw new Error('SLOT_TAKEN'); // ajunge in `error` la subscribe

                const startMin = toMinutes(payload.startTime);
                const created: CalendarAppointment = {
                    id: crypto.randomUUID(),
                    employeeId: employee.id,
                    locationId: payload.locationId,
                    clientName: payload.clientName,
                    service: service.name,
                    start: `${payload.date}T${payload.startTime}:00`,
                    end: `${payload.date}T${toHHmm(startMin + service.durationMinutes)}:00`,
                    status: 'confirmed',
                };

                // datele care nu exista in CalendarAppointment, pentru panoul de detalii
                this.extras[created.id] = {
                    clientPhone: payload.clientPhone,
                    clientEmail: payload.clientEmail,
                    clientNote: payload.clientNote,
                    price: service.price,
                    currency: service.currency,
                    paymentMethod: service.paymentMethod,
                };

                // adauga in lista; signalul anunta calendarul
                this.appointments.update((list) => [...list, created]);
                return created;
            }),
        );
    }


    /** Maps a calendar item to the full details shape (mock until the API exists). */
    getDetails(a: CalendarAppointment): Appointment {
        const employee = this.employees.find((e) => e.id === a.employeeId);
        const minutes = (iso: string) => +iso.slice(11, 13) * 60 + +iso.slice(14, 16);

        return {
            id: a.id,
            businessId: 'studio-armonia',
            serviceId: a.service,
            serviceName: a.service,
            durationMinutes: minutes(a.end) - minutes(a.start),
            clientId: a.id,
             clientName: a.clientName,
             clientPhone: '0700 000 000',
             clientEmail: 'client@example.com',
             employeeId: a.employeeId,
             employeeName: employee?.name ?? '',
             date: a.start.slice(0, 10),
             startTime: a.start.slice(11, 16),
             endTime: a.end.slice(11, 16),
             status: a.status,
             price: 150,
             currency: 'lei',
             paymentMethod: 'Physical',
             isPaid: false,
             ...this.extras[a.id],
            
        };
    }
}