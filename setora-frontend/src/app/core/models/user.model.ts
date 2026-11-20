import { UserRole } from './enums/user-role.model';

export interface User {
  id: number;
  firstName: string;
  lastName: string;
  username: string;
  email: string;
  phoneNumber: string | null;
  role: UserRole;
}
