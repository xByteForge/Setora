import { AppointmentStatus } from "./enums/appointment-status.model";
import { PaymentMethod } from "./enums/payment-method.model";


export interface Appointment {
  id: number;

  // Client: clientId is set for registered users. The DTO fills clientName,
  // clientPhone and clientEmail from the User, or from the ghost-client fields.
  clientId: number | null;
  clientName: string;
  clientPhone: string | null;
  clientEmail: string | null;
  clientNote: string | null;

  employeeId: number;
  employeeName: string;     // DTO only
  
  serviceId: number;
  serviceName: string;      // DTO only
  
  start: string;
  end: string;
  
  status: AppointmentStatus;
  priceAtAppointment: number;
  paymentMethod: PaymentMethod;
  
  createdAt: string;
}