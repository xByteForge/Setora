export type UserRole = 'Admin' | 'BusinessOwner' | 'Employee' | 'Client';

export interface User {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  username: string;
  role: UserRole;
}
