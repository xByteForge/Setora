export type AppointmentStatus = 'confirmed' | 'pending' | 'cancelled' | 'unavailable';
export interface CalendarAppointment {
    id: string;
    employeeId: string;
    locationId: string;
    clientName: string;
    service: string;
    start: string; // ISO, ex. '2026-11-07T09:30:00'
    end: string;
    status: AppointmentStatus;
}