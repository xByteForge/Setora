export type AppointmentStatus = 'confirmed' | 'pending' | 'cancelled' | 'blocked';

export interface SalonLocation {
  id: string;
  name: string;
}

export interface Employee {
  id: string;
  name: string;
  role: string;
  locationIds: string[];
}

export interface Appointment {
  id: string;
  clientName: string;
  service: string;
  employeeId: string;
  locationId: string;
  start: string;
  end: string;
  status: AppointmentStatus;
}