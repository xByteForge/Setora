export type AppointmentStatus = 'confirmed' | 'pending' | 'cancelled' | 'unavailable';
export type PaymentMethod = 'Free' | 'Physical' | 'Online' | 'PhysicalOrOnline';

export interface Appointment {
  id: string;
  businessId: string;
  serviceId: string;
  serviceName: string;
  durationMinutes: number;
  clientId: string;
  clientName: string;
  clientPhone: string;
  clientEmail: string;
  employeeId: string;
  employeeName: string;
  date: string; // YYYY-MM-DD
  startTime: string; // HH:mm
  endTime: string; // HH:mm
  status: AppointmentStatus;
  price: number;
  currency: string;
  paymentMethod: PaymentMethod;
  isPaid: boolean;
  clientNote?: string;
}