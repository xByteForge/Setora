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
import { of, switchMap } from 'rxjs';
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

const ALLOWED_PHOTO_TYPES = ['image/jpeg', 'image/png'];
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
  readonly saveFailed = signal(false);

  readonly selectedPhoto = signal<File | null>(null);
  readonly photoPreviewUrl = signal<string | null>(null);
  readonly photoError = signal<string | null>(null); // cheie de traducere

  
  readonly brokenSrc = signal<string | null>(null);

  
  readonly avatarSrc = computed(() => {
    const src = this.photoPreviewUrl() ?? this.userService.avatarUrl();
    return src && src !== this.brokenSrc() ? src : null;
  });

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

    this.destroyRef.onDestroy(() => this.revokePreview());
  }

  startEditing(): void {
    this.saveFailed.set(false);
    this.editing.set(true);
  }

  cancel(): void {
    this.clearSelectedPhoto();
    this.saveFailed.set(false);
    this.patchForm(this.user());
    this.editing.set(false);
  }

  save(): void {
    if (this.saving()) return;

    const raw = this.form.getRawValue();
    const phone = raw.phoneNumber.trim();

    const payload: ProfileUpdate = {
      firstName: raw.firstName.trim(),
      lastName: raw.lastName.trim(),
      username: raw.username.trim(),
      phoneNumber: phone ? `${raw.countryCode} ${phone}` : '',
    };

    const photo = this.selectedPhoto();

    this.saving.set(true);
    this.saveFailed.set(false);

    this.userService
      .updateProfile(payload)
      .pipe(switchMap(() => (photo ? this.userService.updateProfilePicture(photo) : of(null))))
      .subscribe({
        next: () => {
          this.saving.set(false);
          this.clearSelectedPhoto();
          this.editing.set(false);
        },
        error: () => {
          this.saving.set(false);
          this.saveFailed.set(true);
        },
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