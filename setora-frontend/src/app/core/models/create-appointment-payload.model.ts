export interface CreateAppointmentPayload {
   clientName: string;
    clientPhone: string;
    clientEmail: string;
    serviceId: string;
    locationId: string;
    employeeId: string | null; // null = orice angajat disponibil
    date: string;      // YYYY-MM-DD
    startTime: string; // HH:mm
    clientNote?: string;
}

export type UpdateAppointmentPayload = Pick<
    CreateAppointmentPayload,
    'clientName' | 'clientPhone' | 'clientEmail' | 'clientNote'
>;