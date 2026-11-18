import { Injectable } from '@angular/core';
import { EMPTY, Observable, of } from 'rxjs';
import { User } from '../models/user.model';

export interface ProfileUpdate {
  firstName: string;
  lastName: string;
  username: string;
  phoneNumber: string;
}

export interface PasswordChange {
  currentPassword: string;
  newPassword: string;
}

export type NotificationEvent = 'bookingConfirmed' | 'bookingChanged' | 'reminder';
export type NotificationChannel = 'email' | 'sms' | 'push';
export type NotificationPreferences = Record<NotificationEvent, Record<NotificationChannel, boolean>>;

// Temporary, only so the UI has something to render. Remove when the backend is connected.
const MOCK_USER = {
  id: 1,
  firstName: 'Irina',
  lastName: 'Popescu',
  username: 'irina.popescu',
  email: 'irina@example.com',
  phoneNumber: '700 000 123',
} as User;

@Injectable({ providedIn: 'root' })
export class UserService {
  // TODO: GET /api/me
  getCurrentUser(): Observable<User> {
    return of(MOCK_USER);
  }

  // TODO: PUT /api/me/profile
  updateProfile(_update: ProfileUpdate): Observable<User> {
    return EMPTY;
  }

  // TODO: POST /api/me/avatar (multipart/form-data)
  uploadAvatar(_file: File): Observable<void> {
    return EMPTY;
  }

  // TODO: PUT /api/me/password
  changePassword(_change: PasswordChange): Observable<void> {
    return EMPTY;
  }

  // TODO: GET /api/me/notification-preferences
  getNotificationPreferences(): Observable<NotificationPreferences> {
    return EMPTY;
  }

  // TODO: PUT /api/me/notification-preferences
  updateNotificationPreferences(_prefs: NotificationPreferences): Observable<NotificationPreferences> {
    return EMPTY;
  }

  // TODO: DELETE /api/me
  deleteAccount(): Observable<void> {
    return EMPTY;
  }
}