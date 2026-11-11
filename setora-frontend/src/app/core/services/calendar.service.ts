import { Injectable } from '@angular/core';
import { Employee } from '../models/employee.model';
import { CalendarAppointment } from '../models/calendar-appointment.model';

const DAY = '2026-11-07';
const at = (time: string) => `${DAY}T${time}:00`;

@Injectable({ providedIn: 'root' })
export class CalendarService {
    private readonly employees: Employee[] = [
        { id: 'ana', name: 'Ana Ionescu', initials: 'AI', role: 'Coafură' },
        { id: 'mihai', name: 'Mihai Radu', initials: 'MR', role: 'Frizerie' },
        { id: 'ioana', name: 'Ioana Matei', initials: 'IM', role: 'Îngrijire' },
    ];

    private readonly appointments: CalendarAppointment[] = [
        // Ana
        { id: '1', employeeId: 'ana', clientName: 'Irina Popescu', service: 'Tuns & coafat', start: at('09:30'), end: at('10:30'), status: 'confirmed' },
        { id: '2', employeeId: 'ana', clientName: 'Elena Dobre', service: 'Tuns & coafat', start: at('11:00'), end: at('12:00'), status: 'confirmed' },
        { id: '3', employeeId: 'ana', clientName: 'Maria Șerban', service: 'Vopsit & coafat', start: at('13:00'), end: at('14:30'), status: 'confirmed' },
        // Mihai
        { id: '4', employeeId: 'mihai', clientName: 'Andrei Pavel', service: 'Tuns & barbă', start: at('09:00'), end: at('10:00'), status: 'confirmed' },
        { id: '5', employeeId: 'mihai', clientName: 'Radu Stan', service: 'Tuns & barbă', start: at('10:30'), end: at('11:30'), status: 'pending' },
        { id: '6', employeeId: 'mihai', clientName: 'Pauză de masă', service: 'Interval blocat', start: at('12:00'), end: at('13:00'), status: 'blocked' },
        { id: '7', employeeId: 'mihai', clientName: 'Vlad Marinescu', service: 'Tuns & barbă', start: at('14:00'), end: at('15:00'), status: 'confirmed' },
        // Ioana
        { id: '8', employeeId: 'ioana', clientName: 'Diana Enache', service: 'Manichiură', start: at('10:00'), end: at('11:00'), status: 'confirmed' },
        { id: '9', employeeId: 'ioana', clientName: 'Alina Tudor', service: 'Manichiură', start: at('11:30'), end: at('12:30'), status: 'cancelled' },
        { id: '10', employeeId: 'ioana', clientName: 'Oana Dumitru', service: 'Manichiură', start: at('14:30'), end: at('15:30'), status: 'confirmed' },

        { id: '11', employeeId: 'ana', clientName: 'Laura Matei', service: 'Tuns & coafat', start: '2026-11-02T10:00:00', end: '2026-11-02T11:00:00', status: 'confirmed' },
        { id: '12', employeeId: 'mihai', clientName: 'Paul Ionescu', service: 'Tuns & barbă', start: '2026-11-05T13:00:00', end: '2026-11-05T14:00:00', status: 'pending' },
        { id: '13', employeeId: 'ioana', clientName: 'Sara Popa', service: 'Manichiură', start: '2026-11-05T13:30:00', end: '2026-11-05T14:30:00', status: 'cancelled' },
    ];

    getEmployees(): Employee[] {
        return this.employees;
    }

    getAppointments(): CalendarAppointment[] {
        return this.appointments;
    }
}