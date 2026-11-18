import {
  ChangeDetectionStrategy,
  Component,
  computed,
  effect,
  inject,
  input,
  signal,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { TranslocoModule } from '@ngneat/transloco';
import { User } from '../../../core/models/user.model';
import { UserService } from '../../../core/services/user.service';

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

  readonly user = input.required<User>();
  readonly editing = signal(false);
  readonly imageError = signal(false);

  readonly countries = COUNTRY_OPTIONS;

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
     effect(() => {
      const user = this.user();

      this.patchForm(user);
      this.imageError.set(false);
    });
  }

  startEditing(): void {
    this.editing.set(true);
  }

  cancel(): void {
    this.patchForm(this.user());
    this.editing.set(false);
  }

  save(): void {
    const raw = this.form.getRawValue();
    const cleanPhone = raw.phoneNumber.trim();
    const fullPhoneNumber = cleanPhone ? `${raw.countryCode} ${cleanPhone}` : '';

    const payload = {
      firstName: raw.firstName,
      lastName: raw.lastName,
      username: raw.username,
      phoneNumber: fullPhoneNumber,
    };

    this.userService.updateProfile(payload).subscribe();
    this.editing.set(false);
  }

  changePhoto(): void {
    // TODO: photo upload flow
  }

  private patchForm(u: User): void {
    const phone = u.phoneNumber ?? '';
    const matchedCountry = this.countries.find((c) => phone.startsWith(c.code));

    if (matchedCountry) {
      this.form.reset({
        firstName: u.firstName,
        lastName: u.lastName,
        username: u.username,
        countryCode: matchedCountry.code,
        phoneNumber: phone.replace(matchedCountry.code, '').trim(),
      });
    } else {
      this.form.reset({
        firstName: u.firstName,
        lastName: u.lastName,
        username: u.username,
        countryCode: '+40',
        phoneNumber: phone,
      });
    }
  }
}