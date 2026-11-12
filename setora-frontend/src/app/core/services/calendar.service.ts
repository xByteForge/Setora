import { Injectable } from '@angular/core';
import { Employee } from '../models/employee.model';
import { CalendarAppointment } from '../models/calendar-appointment.model';
import { Appointment, AppointmentStatus } from '../models/appointment.model';

const DAY = '2026-11-07';
const at = (time: string) => `${DAY}T${time}:00`;
const CENTRU = 'buc-centru';
const NORD = 'buc-nord';

@Injectable({ providedIn: 'root' })
export class CalendarService {
    private readonly employees: Employee[] = [
        { id: 'ana', name: 'Ana Ionescu', initials: 'AI', role: 'Coafură', locationIds: [CENTRU] },
        { id: 'mihai', name: 'Mihai Radu', initials: 'MR', role: 'Frizerie', locationIds: [CENTRU] },
        { id: 'ioana', name: 'Ioana Matei', initials: 'IM', role: 'Îngrijire', locationIds: [CENTRU, NORD] },
    ];

    private readonly appointments: CalendarAppointment[] = [
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
    ];

    getEmployees(): Employee[] {
        return this.employees;
    }

    getAppointments(): CalendarAppointment[] {
        return this.appointments;
    }

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

    /** Maps a calendar item to the full details shape (mock until the API exists). */
    getDetails(a: CalendarAppointment): Appointment {
        const employee = this.employees.find((e) => e.id === a.employeeId);
        const minutes = (iso: string) => +iso.slice(11, 13) * 60 + +iso.slice(14, 16);
        const status = (a.status.charAt(0).toUpperCase() + a.status.slice(1)) as AppointmentStatus;

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
             status,
             price: 150,
             currency: 'lei',
             paymentMethod: 'Physical',
             isPaid: false,
             ...this.extras[a.id],
            
        };
    }
}