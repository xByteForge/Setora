export interface Employee {
  id: number;
  businessId: number;
  userId: number;
  active: boolean;
  serviceIds: number[]; 
  name: string;     
  employmentRole: string;    
}