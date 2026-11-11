import { Appointment, Employee, SalonLocation } from '../../core/models/calendar-appointment.model';

// TODO: înlocuiește cu date din serviciu/API.
export const MOCK_LOCATIONS: SalonLocation[] = [
  { id: 'buc-centru', name: 'București · Centru' },
  { id: 'buc-nord', name: 'București · Nord' },
];

export const MOCK_EMPLOYEES: Employee[] = [
  { id: 'ana', name: 'Ana Ionescu', role: 'Coafor', locationIds: ['buc-centru'] },
  { id: 'mihai', name: 'Mihai Radu', role: 'Frizerie', locationIds: ['buc-centru'] },
  { id: 'ioana', name: 'Ioana Matei', role: 'Îngrijire', locationIds: ['buc-centru', 'buc-nord'] },
];

const d = (time: string) => `2026-11-07T${time}:00`;
const loc = 'buc-centru';

export const MOCK_APPOINTMENTS: Appointment[] = [
  { id: '1', clientName: 'Irina Popescu', service: 'Tuns & coafat', employeeId: 'ana', locationId: loc, status: 'confirmed', start: d('09:30'), end: d('10:30') },
  { id: '2', clientName: 'Elena Dobre', service: 'Tuns & coafat', employeeId: 'ana', locationId: loc, status: 'confirmed', start: d('11:00'), end: d('12:00') },
  { id: '3', clientName: 'Maria Șerban', service: 'Vopsit & coafat', employeeId: 'ana', locationId: loc, status: 'confirmed', start: d('13:00'), end: d('14:30') },
  { id: '4', clientName: 'Andrei Pavel', service: 'Tuns & barbă', employeeId: 'mihai', locationId: loc, status: 'confirmed', start: d('09:00'), end: d('10:00') },
  { id: '5', clientName: 'Radu Stan', service: 'Tuns & barbă', employeeId: 'mihai', locationId: loc, status: 'pending', start: d('10:30'), end: d('11:30') },
  { id: '6', clientName: 'Pauză de masă', service: '', employeeId: 'mihai', locationId: loc, status: 'blocked', start: d('12:00'), end: d('13:00') },
  { id: '7', clientName: 'Vlad Marinescu', service: 'Tuns & barbă', employeeId: 'mihai', locationId: loc, status: 'confirmed', start: d('14:00'), end: d('15:00') },
  { id: '8', clientName: 'Diana Enache', service: 'Manichiură', employeeId: 'ioana', locationId: loc, status: 'confirmed', start: d('10:00'), end: d('11:00') },
  { id: '9', clientName: 'Alina Tudor', service: 'Manichiură', employeeId: 'ioana', locationId: loc, status: 'cancelled', start: d('11:30'), end: d('12:30') },
  { id: '10', clientName: 'Oana Dumitru', service: 'Manichiură', employeeId: 'ioana', locationId: loc, status: 'confirmed', start: d('14:30'), end: d('15:30') },
];