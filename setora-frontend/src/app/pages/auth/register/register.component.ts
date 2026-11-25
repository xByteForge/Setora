import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { TranslocoModule } from '@ngneat/transloco';
import { ApiError } from '../../../core/api/api-error.interceptor';
import { AuthService } from '../../../core/services/auth.service';
import { ThemeService } from '../../../core/services/theme.service';
import { LucideSun, LucideMoon } from '@lucide/angular';
import { COUNTRY_OPTIONS } from '../../../core/constants/countries';

type RegisterField =
  | 'firstName'
  | 'lastName'
  | 'username'
  | 'phoneNumber'
  | 'email'
  | 'confirmEmail'
  | 'password'
  | 'confirmPassword'
  | 'acceptTerms';


function strongPassword(control: AbstractControl): ValidationErrors | null {
  const value: string = control.value ?? '';
  if (!value) return null; // lipsa valorii e tratată de Validators.required

  const errors: ValidationErrors = {};
  if (value.length < 8 || value.length > 64) errors['length'] = true;
  if (!/[A-Z]/.test(value)) errors['uppercase'] = true;
  if (!/[a-z]/.test(value)) errors['lowercase'] = true;
  if (!/[^A-Za-z0-9]/.test(value)) errors['special'] = true;

  return Object.keys(errors).length > 0 ? errors : null;
}

function fieldsMatch(group: AbstractControl): ValidationErrors | null {
  const errors: ValidationErrors = {};
  const email = group.get('email')?.value;
  const confirmEmail = group.get('confirmEmail')?.value;
  const password = group.get('password')?.value;
  const confirmPassword = group.get('confirmPassword')?.value;

  if (confirmEmail && email !== confirmEmail) errors['emailMismatch'] = true;
  if (confirmPassword && password !== confirmPassword) errors['passwordMismatch'] = true;

  return Object.keys(errors).length > 0 ? errors : null;
}

@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule, RouterLink, TranslocoModule, LucideSun, LucideMoon],
  templateUrl: './register.component.html',
  styleUrls: ['../auth-shared.css'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RegisterComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  readonly theme = inject(ThemeService);

  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  readonly emailTaken = signal(false);
  readonly showPassword = signal(false);
  readonly showConfirm = signal(false);

  readonly countries = COUNTRY_OPTIONS;

  readonly form = this.fb.nonNullable.group(
    {
      firstName: ['', [Validators.required, Validators.maxLength(100)]],
      lastName: ['', [Validators.required, Validators.maxLength(100)]],
      username: ['', [Validators.required, Validators.maxLength(100)]],
      countryCode: ['+40'],
      phoneNumber: ['', [Validators.maxLength(30)]],
      email: ['', [Validators.required, Validators.email, Validators.maxLength(255)]],
      confirmEmail: ['', [Validators.required]],
      password: ['', [Validators.required, strongPassword]],
      confirmPassword: ['', [Validators.required]],
      acceptTerms: [false, [Validators.requiredTrue]],
    },
    { validators: fieldsMatch },
  );

  isInvalid(name: RegisterField): boolean {
    const control = this.form.controls[name];
    return control.invalid && control.touched;
  }

  passwordError(): string | null {
    const control = this.form.controls.password;
    if (!control.touched) return null;
    if (control.hasError('required')) return 'AUTH.REGISTER.REQUIRED';
    if (control.hasError('length')) return 'AUTH.REGISTER.PASSWORD_LENGTH';
    if (control.hasError('uppercase')) return 'AUTH.REGISTER.PASSWORD_UPPERCASE';
    if (control.hasError('lowercase')) return 'AUTH.REGISTER.PASSWORD_LOWERCASE';
    if (control.hasError('special')) return 'AUTH.REGISTER.PASSWORD_SPECIAL';
    return null;
  }

  submit(): void {
    this.error.set(null);
    this.emailTaken.set(false);
    this.form.markAllAsTouched();
    if (this.form.invalid || !this.form.controls.acceptTerms.value || this.loading()) return;

    const v = this.form.getRawValue();
    const phone = v.phoneNumber.trim();

    this.loading.set(true);
    this.auth
      .register({
        firstName: v.firstName.trim(),
        lastName: v.lastName.trim(),
        username: v.username.trim(),
        email: v.email.trim(),
        phoneNumber: phone ? `${v.countryCode} ${phone}` : null,
        password: v.password,
      })
      .subscribe({
        next: () => void this.router.navigateByUrl('/'),
        error: (err: ApiError) => {
          this.loading.set(false);
          if (err.status === 409) {
            this.emailTaken.set(true);
          } else {
            this.error.set('ERRORS.GENERIC');
          }
        },
      });
  }
}