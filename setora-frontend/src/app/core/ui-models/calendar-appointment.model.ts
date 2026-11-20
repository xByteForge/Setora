import { Appointment } from '../models/appointment.model';

export type CalendarAppointment = Pick<
  Appointment,
  'id' | 'employeeId' | 'serviceId' | 'serviceName' | 'clientName' | 'status' | 'start' | 'end'
>;
