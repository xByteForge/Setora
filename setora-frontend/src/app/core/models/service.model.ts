import { PaymentMethod } from './enums/payment-method.model';
import { ServiceType } from './service-type.model';

export interface Service {
  id: number;
  businessId: number;
  name: string;
  description: string | null;
  type: ServiceType;
  price: number;
  durationMinutes: number;
  active: boolean;
  paymentMethod: PaymentMethod;
}
