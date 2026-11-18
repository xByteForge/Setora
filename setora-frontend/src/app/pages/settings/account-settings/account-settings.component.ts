import { ChangeDetectionStrategy, Component, effect, inject, input, output, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { TranslocoModule } from '@ngneat/transloco';
import { User } from '../../../core/models/user.model';
import {
  NotificationChannel,
  NotificationEvent,
  UserNotificationPreference,
} from '../../../core/models/enums/notification-type.model';
import { UserService } from '../../../core/services/user.service';
import { ThemeService, Theme } from '../../../core/services/theme.service';

export type ThemeMode = 'light' | 'dark';
type PasswordField = 'current' | 'new' | 'confirm';

const NOTIFICATION_ROWS: { event: NotificationEvent; titleKey: string; hintKey: string }[] = [
  { event: 'BOOKING_CONFIRMED', titleKey: 'SETTINGS.NOTIFICATIONS.CONFIRMED', hintKey: 'SETTINGS.NOTIFICATIONS.CONFIRMED_HINT' },
  { event: 'BOOKING_CHANGED', titleKey: 'SETTINGS.NOTIFICATIONS.CHANGED', hintKey: 'SETTINGS.NOTIFICATIONS.CHANGED_HINT' },
  { event: 'REMINDER', titleKey: 'SETTINGS.NOTIFICATIONS.REMINDER', hintKey: 'SETTINGS.NOTIFICATIONS.REMINDER_HINT' },
];

const CHANNELS: { id: NotificationChannel; labelKey: string }[] = [
  { id: 'EMAIL', labelKey: 'SETTINGS.NOTIFICATIONS.EMAIL' },
  { id: 'SMS', labelKey: 'SETTINGS.NOTIFICATIONS.SMS' },
];

const THEME_OPTIONS: { value: ThemeMode; labelKey: string }[] = [
  { value: 'light', labelKey: 'SETTINGS.APPEARANCE.LIGHT' },
  { value: 'dark', labelKey: 'SETTINGS.APPEARANCE.DARK' },
];

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

  readonly notifications = signal<UserNotificationPreference[]>([]);

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

  constructor() {
    effect(() => {
      const u = this.user();
      this.notifications.set(u.notificationPreferences ?? []);
    });
  }

  changeLoginEmail(): void {
    // TODO: login email change flow
  }

  toggleVisible(field: PasswordField): void {
    this.visible.update((v) => ({ ...v, [field]: !v[field] }));
  }

  changePassword(): void {
    const { currentPassword, newPassword } = this.passwordForm.getRawValue();
    this.userService.changePassword({ currentPassword, newPassword }).subscribe();
  }

  isChecked(event: NotificationEvent, channel: NotificationChannel): boolean {
    return this.notifications().some((p) => p.event === event && p.channel === channel);
  }

  toggleNotification(event: NotificationEvent, channel: NotificationChannel): void {
    this.notifications.update((prefs) => {
      const exists = prefs.some((p) => p.event === event && p.channel === channel);
      const updated = exists
        ? prefs.filter((p) => !(p.event === event && p.channel === channel))
        : [...prefs, { event, channel }];

      this.userService.updateNotificationPreferences(updated).subscribe();
      return updated;
    });
  }

  deleteAccount(): void {
    this.userService.deleteAccount().subscribe();
  }
}