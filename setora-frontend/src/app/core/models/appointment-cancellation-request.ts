import { CancellationRequestStatus } from './enums/cancellation-request-status.model';

export interface AppointmentCancellationRequest {
  id: number;
  appointmentId: number;
  employeeId: number;
  reason: string;
  status: CancellationRequestStatus;
  createdAt: string;
  reviewedAt: string | null;
}
