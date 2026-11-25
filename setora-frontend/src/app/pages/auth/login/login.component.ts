import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { TranslocoModule } from '@ngneat/transloco';
import { LucideMoon, LucideSun } from '@lucide/angular';
import { ApiError } from '../../../core/api/api-error.interceptor';
import { AuthService } from '../../../core/services/auth.service';
import { ThemeService } from '../../../core/services/theme.service';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink, TranslocoModule, LucideSun, LucideMoon],
  templateUrl: './login.component.html',
  styleUrls: ['../auth-shared.css'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  readonly theme = inject(ThemeService);

  readonly loading = signal(false);
  
  readonly error = signal<string | null>(null);
  readonly showPassword = signal(false);
  readonly sessionExpired = this.route.snapshot.queryParamMap.get('reason') === 'expired';

  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required],
    rememberMe: [false],
  });

  isInvalid(name: 'email' | 'password'): boolean {
    const control = this.form.controls[name];
    return control.invalid && control.touched;
  }

  submit(): void {
    this.error.set(null);
    this.form.markAllAsTouched();
    if (this.form.invalid || this.loading()) return;

    this.loading.set(true);
    const { email, password, rememberMe } = this.form.getRawValue();

    this.auth.login({ email: email.trim(), password }, rememberMe).subscribe({
      next: () => void this.router.navigateByUrl(this.safeReturnUrl()),
      error: (err: ApiError) => {
        this.loading.set(false);
        this.error.set(err.isUnauthorized ? 'AUTH.LOGIN.INVALID_CREDENTIALS' : 'ERRORS.GENERIC');
      },
    });
  }

  
  private safeReturnUrl(): string {
    const url = this.route.snapshot.queryParamMap.get('returnUrl');
    return url && url.startsWith('/') && !url.startsWith('//') ? url : '/';
  }
}