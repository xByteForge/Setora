export interface CreateAppointmentPayload {
  clientName: string;
  clientPhone: string;
  clientEmail: string;
  serviceId: number;
  employeeId: number | null; // null = any available employee
  date: string; // YYYY-MM-DD, in the business timezone
  startTime: string; // HH:mm, in the business timezone
  clientNote?: string;
}
