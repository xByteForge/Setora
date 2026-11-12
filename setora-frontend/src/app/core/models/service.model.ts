import { PaymentMethod } from './appointment.model';

export interface Service {
    id: string;
    name: string;
    durationMinutes: number;
    price: number;
    currency: string;
    paymentMethod: PaymentMethod;
}
