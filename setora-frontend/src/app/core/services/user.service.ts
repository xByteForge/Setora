import { Injectable, signal } from '@angular/core';
import { Observable, of } from 'rxjs';
import { User } from '../models/user.model';
import { UserRole } from '../models/enums/user-role.model';
import { UserNotificationPreference } from '../models/enums/notification-type.model';

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

const INITIAL_USER: User = {
  id: 1,
  firstName: 'Irina',
  lastName: 'Popescu',
  username: 'irina.popescu',
  email: 'irina@example.com',
  phoneNumber: '+40 700 000 123',
  role: 'ADMIN' as UserRole,
  profilePictureUrl: null,
  notificationPreferences: [
    { event: 'BOOKING_CONFIRMED', channel: 'EMAIL' },
    { event: 'BOOKING_CONFIRMED', channel: 'SMS' },
    { event: 'BOOKING_CHANGED', channel: 'EMAIL' },
    { event: 'REMINDER', channel: 'SMS' },
  ],
};

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly currentUserSignal = signal<User>(INITIAL_USER);

  // TODO: GET /api/me
  getCurrentUser(): Observable<User> {
    return of(this.currentUserSignal());
  }

  // TODO: PUT /api/me/profile
  updateProfile(update: ProfileUpdate): Observable<User> {
    const updatedUser: User = {
      ...this.currentUserSignal(),
      ...update,
    };
    this.currentUserSignal.set(updatedUser);
    return of(updatedUser);
  }

  // TODO: POST /api/me/avatar (multipart/form-data)
  uploadAvatar(file: File): Observable<string> {
    const previewUrl = URL.createObjectURL(file);
    this.currentUserSignal.update((u) => ({ ...u, profilePictureUrl: previewUrl }));
    return of(previewUrl);
  }

  // TODO: PUT /api/me/password
  changePassword(_change: PasswordChange): Observable<void> {
    return of(void 0);
  }

  // TODO: GET /api/me/notification-preferences
  getNotificationPreferences(): Observable<UserNotificationPreference[]> {
    return of(this.currentUserSignal().notificationPreferences ?? []);
  }

  // TODO: PUT /api/me/notification-preferences
  updateNotificationPreferences(prefs: UserNotificationPreference[]): Observable<UserNotificationPreference[]> {
    this.currentUserSignal.update((u) => ({ ...u, notificationPreferences: prefs }));
    return of(prefs);
  }

  // TODO: DELETE /api/me
  deleteAccount(): Observable<void> {
    return of(void 0);
  }
}