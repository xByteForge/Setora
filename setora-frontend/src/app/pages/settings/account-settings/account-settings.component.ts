import { ChangeDetectionStrategy, Component, inject, input, output, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { TranslocoModule } from '@ngneat/transloco';
import { User } from '../../../core/models/user.model';
import {
  NotificationChannel,
  NotificationEvent,
  NotificationPreferences,
  UserService,
} from '../../../core/services/user.service';
import { ThemeService, Theme } from '../../../core/services/theme.service';

export type ThemeMode = 'light' | 'dark';
type PasswordField = 'current' | 'new' | 'confirm';

const NOTIFICATION_ROWS: { event: NotificationEvent; titleKey: string; hintKey: string }[] = [
  { event: 'bookingConfirmed', titleKey: 'SETTINGS.NOTIFICATIONS.CONFIRMED', hintKey: 'SETTINGS.NOTIFICATIONS.CONFIRMED_HINT' },
  { event: 'bookingChanged', titleKey: 'SETTINGS.NOTIFICATIONS.CHANGED', hintKey: 'SETTINGS.NOTIFICATIONS.CHANGED_HINT' },
  { event: 'reminder', titleKey: 'SETTINGS.NOTIFICATIONS.REMINDER', hintKey: 'SETTINGS.NOTIFICATIONS.REMINDER_HINT' },
];

const CHANNELS: { id: NotificationChannel; labelKey: string }[] = [
  { id: 'email', labelKey: 'SETTINGS.NOTIFICATIONS.EMAIL' },
  { id: 'sms', labelKey: 'SETTINGS.NOTIFICATIONS.SMS' },
  { id: 'push', labelKey: 'SETTINGS.NOTIFICATIONS.PUSH' },
];

const THEME_OPTIONS: { value: ThemeMode; labelKey: string }[] = [
  { value: 'light', labelKey: 'SETTINGS.APPEARANCE.LIGHT' },
  { value: 'dark', labelKey: 'SETTINGS.APPEARANCE.DARK' },
];

// Starting values only, until the backend provides the real preferences.
const DEFAULT_NOTIFICATIONS: NotificationPreferences = {
  bookingConfirmed: { email: true, sms: true, push: true },
  bookingChanged: { email: true, sms: false, push: true },
  reminder: { email: false, sms: true, push: true },
};

@Component({
  selector: 'app-account-settings',
  imports: [ReactiveFormsModule, TranslocoModule],
  templateUrl: './account-settings.component.html',
  styleUrls: ['../settings-shared.css', './account-settings.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AccountSettingsComponent {
  private readonly fb = inject(FormBuilder);
  private readonly userService = inject(UserService);

  readonly user = input.required<User>();
  readonly openProfile = output<void>();

  readonly channels = CHANNELS;
  readonly notificationRows = NOTIFICATION_ROWS;
  readonly themeOptions = THEME_OPTIONS;

  private readonly themeService = inject(ThemeService);
  readonly theme = this.themeService.currentTheme;

  setTheme(mode: Theme): void {
    this.themeService.currentTheme.set(mode);

  }


  readonly notifications = signal<NotificationPreferences>(DEFAULT_NOTIFICATIONS);

  readonly passwordForm = this.fb.nonNullable.group({
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
  });
  readonly visible = signal<Record<PasswordField, boolean>>({
    current: false,
    new: false,
    confirm: false,
  });

  readonly confirmingDelete = signal(false);



  changeLoginEmail(): void {
    // TODO: login email change flow (not in the design yet)
  }



  toggleVisible(field: PasswordField): void {
    this.visible.update((v) => ({ ...v, [field]: !v[field] }));
  }

  changePassword(): void {
    const { currentPassword, newPassword } = this.passwordForm.getRawValue();
    this.userService.changePassword({ currentPassword, newPassword }).subscribe();
  }


  isChecked(event: NotificationEvent, channel: NotificationChannel): boolean {
    return this.notifications()[event][channel];
  }

  toggleNotification(event: NotificationEvent, channel: NotificationChannel): void {
    this.notifications.update((n) => ({
      ...n,
      [event]: { ...n[event], [channel]: !n[event][channel] },
    }));
    this.userService.updateNotificationPreferences(this.notifications()).subscribe();
  }



  deleteAccount(): void {
    this.userService.deleteAccount().subscribe();
    // TODO: clear the session and redirect to login once auth is in place
  }
}