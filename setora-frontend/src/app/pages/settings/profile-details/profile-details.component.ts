import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  effect,
  inject,
  input,
  signal,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { TranslocoModule } from '@ngneat/transloco';
import { Observable, of } from 'rxjs';
import { User } from '../../../core/models/user.model';
import { ProfileUpdate, UserService } from '../../../core/services/user.service';

export interface CountryOption {
  code: string;
  flag: string;
  name: string;
}

const COUNTRY_OPTIONS: CountryOption[] = [
  { code: '+40', flag: '🇷🇴', name: 'Romania' },
  { code: '+373', flag: '🇲🇩', name: 'Moldova' },
  { code: '+44', flag: '🇬🇧', name: 'UK' },
  { code: '+1', flag: '🇺🇸', name: 'USA / Canada' },
  { code: '+33', flag: '🇫🇷', name: 'France' },
  { code: '+49', flag: '🇩🇪', name: 'Germany' },
  { code: '+39', flag: '🇮🇹', name: 'Italy' },
  { code: '+34', flag: '🇪🇸', name: 'Spain' },
];

// Aceleași formate ca în backend
const ALLOWED_PHOTO_TYPES = ['image/jpeg', 'image/png', 'image/webp'];
const MAX_PHOTO_BYTES = 5 * 1024 * 1024;

@Component({
  selector: 'app-profile-details',
  imports: [ReactiveFormsModule, TranslocoModule],
  templateUrl: './profile-details.component.html',
  styleUrls: ['../settings-shared.css', './profile-details.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProfileDetailsComponent {
  private readonly fb = inject(FormBuilder);
  private readonly userService = inject(UserService);
  private readonly destroyRef = inject(DestroyRef);

  readonly user = input.required<User>();
  readonly countries = COUNTRY_OPTIONS;

  readonly editing = signal(false);
  readonly saving = signal(false);
  readonly saveError = signal<string | null>(null);

  readonly selectedPhoto = signal<File | null>(null);
  readonly photoPreviewUrl = signal<string | null>(null);
  readonly photoError = signal<string | null>(null);

  private readonly avatarSource = computed(
    () => this.photoPreviewUrl() ?? this.userService.avatarUrl(),
  );

  readonly imageFailed = signal(false);

  readonly avatarSrc = computed(() => (this.imageFailed() ? null : this.avatarSource()));

  readonly initials = computed(() => {
    const u = this.user();
    return `${u.firstName.charAt(0)}${u.lastName.charAt(0)}`.toUpperCase();
  });

  readonly form = this.fb.nonNullable.group({
    firstName: '',
    lastName: '',
    username: '',
    countryCode: '+40',
    phoneNumber: '',
  });

  constructor() {
    effect(() => this.patchForm(this.user()));

    effect(() => {
      this.avatarSource();
      this.imageFailed.set(false);
    });

    this.destroyRef.onDestroy(() => this.revokePreview());
  }

  startEditing(): void {
    this.saveError.set(null);
    this.editing.set(true);
  }

  cancel(): void {
    this.clearSelectedPhoto();
    this.saveError.set(null);
    this.patchForm(this.user());
    this.editing.set(false);
  }

  save(): void {
    if (this.saving()) return;

    this.saving.set(true);
    this.saveError.set(null);

    const profile$: Observable<unknown> = this.form.dirty
      ? this.userService.updateProfile(this.buildPayload())
      : of(null);

    profile$.subscribe({
      next: () => this.uploadPhotoIfSelected(),
      error: () => this.finishWithError('ERRORS.GENERIC'),
    });
  }

  onPhotoSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    input.value = ''; 

    if (!file) return;

    if (!ALLOWED_PHOTO_TYPES.includes(file.type)) {
      this.photoError.set('SETTINGS.PROFILE.PHOTO_TYPE_ERROR');
      return;
    }

    if (file.size > MAX_PHOTO_BYTES) {
      this.photoError.set('SETTINGS.PROFILE.PHOTO_SIZE_ERROR');
      return;
    }

    this.photoError.set(null);
    this.revokePreview();
    this.selectedPhoto.set(file);
    this.photoPreviewUrl.set(URL.createObjectURL(file));
  }

  private uploadPhotoIfSelected(): void {
    const photo = this.selectedPhoto();

    if (!photo) {
      this.finishSave();
      return;
    }

    this.userService.updateProfilePicture(photo).subscribe({
      next: () => this.finishSave(),
      error: () => this.finishWithError('SETTINGS.PROFILE.PHOTO_UPLOAD_ERROR'),
    });
  }

  private finishSave(): void {
    this.saving.set(false);
    this.clearSelectedPhoto();
    this.editing.set(false);
  }

  private finishWithError(key: string): void {
    this.saving.set(false);
    this.saveError.set(key);
  }

  private buildPayload(): ProfileUpdate {
    const raw = this.form.getRawValue();
    const phone = raw.phoneNumber.trim();

    return {
      firstName: raw.firstName.trim(),
      lastName: raw.lastName.trim(),
      username: raw.username.trim(),
      phoneNumber: phone ? `${raw.countryCode} ${phone}` : '',
    };
  }

  private patchForm(u: User): void {
    const phone = u.phoneNumber ?? '';
    const matched = this.countries.find((c) => phone.startsWith(c.code));

    this.form.reset({
      firstName: u.firstName,
      lastName: u.lastName,
      username: u.username,
      countryCode: matched?.code ?? '+40',
      phoneNumber: matched ? phone.slice(matched.code.length).trim() : phone,
    });
  }

  private clearSelectedPhoto(): void {
    this.revokePreview();
    this.selectedPhoto.set(null);
    this.photoError.set(null);
  }

  private revokePreview(): void {
    const url = this.photoPreviewUrl();
    if (url) URL.revokeObjectURL(url);
    this.photoPreviewUrl.set(null);
  }
}