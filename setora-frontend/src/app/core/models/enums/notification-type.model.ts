export type NotificationChannel = 'EMAIL' | 'SMS';

export type NotificationEvent = 'BOOKING_CONFIRMED' | 'BOOKING_CHANGED' | 'REMINDER';

export interface UserNotificationPreference {
  event: NotificationEvent;
  channel: NotificationChannel;
}