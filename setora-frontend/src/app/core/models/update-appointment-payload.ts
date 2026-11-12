import { CreateAppointmentPayload } from "./create-appointment-payload.model";

export type UpdateAppointmentPayload = Pick<
    CreateAppointmentPayload,
    'clientName' | 'clientPhone' | 'clientEmail' | 'clientNote'
>;