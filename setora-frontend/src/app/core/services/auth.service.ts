import { computed, inject, Injectable, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { AuthApi } from '../api/auth.api';
import { environment } from '../../../environment';
import { AuthResponse } from '../ui-models/auth.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly authApi = inject(AuthApi);
  private readonly session = signal<AuthResponse | null>(null);

  readonly accessToken = computed(() => this.session()?.accessToken ?? null);

  async autoLogin(): Promise<AuthResponse> {
    const { email, password } = environment.devAuth;
    const response = await firstValueFrom(this.authApi.login({ email, password }));
    this.session.set(response);
    return response;
  }
}
