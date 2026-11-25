import { inject, Injectable, signal } from '@angular/core';
import { catchError, map, Observable, of, switchMap, tap, throwError } from 'rxjs';
import { UserApi } from '../api/user.api';
import { User } from '../models/user.model';
import { UserNotificationPreference } from '../models/enums/notification-type.model';
import { EmailChange } from '../ui-models/email-change.model';
import { ApiError } from '../api/api-error.interceptor';

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

const DEFAULT_NOTIFICATION_PREFERENCES: UserNotificationPreference[] = [
  { event: 'APPOINTMENT_CONFIRMED', channel: 'EMAIL' },
  { event: 'APPOINTMENT_CONFIRMED', channel: 'SMS' },
  { event: 'APPOINTMENT_CHANGED', channel: 'EMAIL' },
  { event: 'REMINDER', channel: 'SMS' },
];

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly api = inject(UserApi);

  private readonly userSignal = signal<User | null>(null);
  private readonly avatarSignal = signal<string | null>(null);
  private readonly preferencesSignal = signal<UserNotificationPreference[]>(
    DEFAULT_NOTIFICATION_PREFERENCES,
  );
  private avatarObjectUrl: string | null = null;

  readonly currentUser = this.userSignal.asReadonly();
  readonly avatarUrl = this.avatarSignal.asReadonly();

  setCurrentUser(user: User): void {
    this.userSignal.set(user);
  }

  loadCurrentUser(): Observable<User> {
    return this.api.getMe().pipe(tap((user) => this.userSignal.set(user)));
  }

  loadAvatar(userId: number): Observable<string | null> {
    return this.api.getAvatar(userId).pipe(
      map((blob) => this.replaceAvatar(blob)),
      catchError((error: unknown) => {
        if (error instanceof ApiError && error.status === 404) {
          this.clearAvatar();
          return of(null);
        }
        return throwError(() => error);
      }),
    );
  }

  updateProfilePicture(file: File): Observable<string | null> {
    const user = this.userSignal();
    if (!user) {
      return throwError(() => new Error('Current user is not loaded yet.'));
    }

    return this.api.uploadAvatar(file).pipe(switchMap(() => this.loadAvatar(user.id)));
  }

  updateProfile(update: ProfileUpdate): Observable<User> {
    const current = this.requireUser();
    return this.api
      .updateMe({
        firstName: update.firstName,
        lastName: update.lastName,
        username: update.username,
        email: current.email,
        phoneNumber: update.phoneNumber || null,
      })
      .pipe(tap((user) => this.userSignal.set(user)));
  }

  changeEmail(change: EmailChange): Observable<User> {
    const current = this.requireUser();
    return this.api
      .updateMe({
        firstName: current.firstName,
        lastName: current.lastName,
        username: current.username,
        email: change.newEmail,
        phoneNumber: current.phoneNumber,
      })
      .pipe(tap((user) => this.userSignal.set(user)));
  }

  changePassword(change: PasswordChange): Observable<void> {
    return this.api.changePassword(change);
  }

  getNotificationPreferences(): Observable<UserNotificationPreference[]> {
    return of(this.preferencesSignal());
  }

  updateNotificationPreferences(
    prefs: UserNotificationPreference[],
  ): Observable<UserNotificationPreference[]> {
    this.preferencesSignal.set(prefs);
    return of(prefs);
  }

  deleteAccount(): Observable<void> {
    return of(void 0);
  }

  private replaceAvatar(blob: Blob): string {
    if (this.avatarObjectUrl) URL.revokeObjectURL(this.avatarObjectUrl);
    this.avatarObjectUrl = URL.createObjectURL(blob);
    this.avatarSignal.set(this.avatarObjectUrl);
    return this.avatarObjectUrl;
  }

  private clearAvatar(): void {
    if (this.avatarObjectUrl) URL.revokeObjectURL(this.avatarObjectUrl);
    this.avatarObjectUrl = null;
    this.avatarSignal.set(null);
  }

  private requireUser(): User {
    const user = this.userSignal();
    if (!user) throw new Error('Current user is not loaded yet.');
    return user;
  }
}
