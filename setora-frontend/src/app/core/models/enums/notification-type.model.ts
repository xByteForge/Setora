export type NotificationChannel = 'EMAIL' | 'SMS';
export type NotificationEvent = 'APPOINTMENT_CONFIRMED' | 'APPOINTMENT_CHANGED' | 'REMINDER';

export interface UserNotificationPreference {
  event: NotificationEvent;
  channel: NotificationChannel;
}