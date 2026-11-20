import { ChangeDetectionStrategy, Component, effect, inject, input, output, signal } from '@angular/core';
import { TranslocoModule } from '@ngneat/transloco';
import { User } from '../../../core/models/user.model';
import {
  NotificationChannel,
  NotificationEvent,
  UserNotificationPreference,
} from '../../../core/models/enums/notification-type.model';
import { UserService } from '../../../core/services/user.service';
import { ThemeService, Theme } from '../../../core/services/theme.service';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';

export type ThemeMode = 'light' | 'dark';
type PasswordField = 'current' | 'new' | 'confirm';

const NOTIFICATION_ROWS: { event: NotificationEvent; titleKey: string; hintKey: string }[] = [
  { event: 'APPOINTMENT_CONFIRMED', titleKey: 'SETTINGS.NOTIFICATIONS.CONFIRMED', hintKey: 'SETTINGS.NOTIFICATIONS.CONFIRMED_HINT' },
  { event: 'APPOINTMENT_CHANGED', titleKey: 'SETTINGS.NOTIFICATIONS.CHANGED', hintKey: 'SETTINGS.NOTIFICATIONS.CHANGED_HINT' },
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

  readonly showEmailForm = signal(false);
  readonly emailLoading = signal(false);
  readonly emailSuccess = signal(false);
  readonly emailError = signal('');

  readonly deleteForm = this.fb.nonNullable.group({
    currentPassword: ['', Validators.required],
  });

  readonly deleteLoading = signal(false);
  readonly deleteError = signal('');

  setTheme(mode: Theme): void {
    this.themeService.currentTheme.set(mode);
  }

  readonly notifications = signal<UserNotificationPreference[]>([]);

  
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
    this.emailForm.markAllAsTouched();
    this.emailSuccess.set(false);
    this.emailError.set('');

    if (this.emailForm.invalid) {
      return;
    }

    const { newEmail, confirmEmail, currentPassword } =
      this.emailForm.getRawValue();

    if (newEmail.trim().toLowerCase() !== confirmEmail.trim().toLowerCase()) {
      this.emailError.set('Email addresses do not match.');
      return;
    }

    if (newEmail.trim().toLowerCase() === this.user().email.toLowerCase()) {
      this.emailError.set('Your new email must be different from your current email.');
      return;
    }

    this.emailLoading.set(true);

    this.userService.changeEmail({
      newEmail: newEmail.trim(),
      currentPassword,
    }).subscribe({
      next: () => {
        this.emailLoading.set(false);
        this.emailSuccess.set(true);
        this.showEmailForm.set(false);
        this.emailForm.reset();
      },
      error: () => {
        this.emailLoading.set(false);
        this.emailError.set('Unable to update your email. Please check your password and try again.');
      },
    });
  }

  cancelEmailChange(): void {
    this.showEmailForm.set(false);
    this.emailForm.reset();
    this.emailError.set('');
    this.emailSuccess.set(false);
  }

  toggleVisible(field: PasswordField): void {
    this.visible.update((v) => ({ ...v, [field]: !v[field] }));
  }

  readonly passwordForm = this.fb.nonNullable.group({
    currentPassword: ['', Validators.required],
    newPassword: ['', [
      Validators.required,
      Validators.minLength(8),
    ]],
    confirmPassword: ['', Validators.required],
  });

  readonly passwordLoading = signal(false);
  readonly passwordSuccess = signal(false);
  readonly passwordError = signal('');

changePassword(): void {
    this.passwordForm.markAllAsTouched();
    this.passwordSuccess.set(false);
    this.passwordError.set('');

    if (this.passwordForm.invalid) {
      return;
    }

    const { currentPassword, newPassword, confirmPassword } =
      this.passwordForm.getRawValue();

    if (newPassword !== confirmPassword) {
      this.passwordError.set('Passwords do not match.');
      return;
    }

    if (currentPassword === newPassword) {
      this.passwordError.set('New password must be different from the current password.');
      return;
    }

    this.passwordLoading.set(true);

    this.userService.changePassword({ currentPassword, newPassword })
      .pipe(finalize(() => this.passwordLoading.set(false)))
      .subscribe({
        next: () => {
          this.passwordSuccess.set(true);
          this.passwordForm.reset();
          this.visible.set({
            current: false,
            new: false,
            confirm: false,
          });
        },
        error: () => {
          this.passwordError.set('Unable to change password. Please check your current password and try again.');
        },
      });
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
    this.deleteForm.markAllAsTouched();
    this.deleteError.set('');

    if (this.deleteForm.invalid) {
      return;
    }

    const { currentPassword } = this.deleteForm.getRawValue();

    this.deleteLoading.set(true);

    this.userService.deleteAccount().subscribe({
      next: () => {
        this.deleteLoading.set(false);

      },
      error: () => {
        this.deleteLoading.set(false);
        this.deleteError.set(
          'Unable to delete your account. Please check your password and try again.'
        );
      },
    });
  }

  cancelDelete(): void {
    this.confirmingDelete.set(false);
    this.deleteForm.reset();
    this.deleteError.set('');
    this.visible.update(v => ({ ...v, current: false }));
  }

  

  readonly emailForm = this.fb.nonNullable.group({
    newEmail: ['', [Validators.required, Validators.email]],
    confirmEmail: ['', [Validators.required, Validators.email]],
    currentPassword: ['', Validators.required],
  });
}