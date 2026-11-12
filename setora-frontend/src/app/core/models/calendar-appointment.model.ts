export type AppointmentStatus = 'confirmed' | 'pending' | 'cancelled' | 'unavailable';
export interface CalendarAppointment {
    id: string;
    employeeId: string;
    locationId: string;
    serviceId: string;  
    clientName: string;
    service: string;
    start: string;
    end: string;
    status: AppointmentStatus;
}