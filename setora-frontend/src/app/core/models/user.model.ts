import { UserRole } from "./enums/user-role.model";
import { UserNotificationPreference } from "./enums/notification-type.model";

export interface User {
  id: number;
  firstName: string;
  lastName: string;
  username: string;
  email: string;
  phoneNumber: string | null;
  role: UserRole;
  notificationPreferences?: UserNotificationPreference[]; 
}