import { PaymentMethod } from "./enums/payment-method.model";


export interface Service {
  id: number;
  businessId: number;
  name: string;
  description: string | null;
  price: number;
  durationMinutes: number;
  active: boolean;
  paymentMethod: PaymentMethod;
}