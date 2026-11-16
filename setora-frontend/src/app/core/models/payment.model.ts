import { PaymentMethod } from "./enums/payment-method.model";
import { PaymentStatus } from "./enums/payment-status.model";


export interface Payment {
  id: number;
  amount: number;
  method: PaymentMethod;
  status: PaymentStatus;
  transactionId: string | null;
  createdAt: string;
  paidAt: string | null;
  refundedAmount: number;
  refundedAt: string | null;
}