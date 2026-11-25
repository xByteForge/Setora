import { isPlatformBrowser } from '@angular/common';
import { computed, inject, Injectable, PLATFORM_ID, signal } from '@angular/core';
import { Router } from '@angular/router';
import {
  catchError,
  finalize,
  firstValueFrom,
  map,
  Observable,
  of,
  shareReplay,
  switchMap,
  tap,
  throwError,
} from 'rxjs';
import { AuthApi } from '../api/auth.api';
import { AuthResponse, LoginRequest, RegisterRequest } from '../ui-models/auth.model';
import { UserService } from './user.service';

interface Session {
  accessToken: string;
  refreshToken: string;
  /** Momentul expirării access token-ului, în milisecunde de la epoch. */
  expiresAt: number;
  /** Dacă e true, refresh token-ul e păstrat în localStorage. */
  rememberMe: boolean;
}

const STORAGE_KEY = 'setora_refresh_token';
/** Reîmprospătăm access token-ul cu 60 de secunde înainte de expirare. */
const REFRESH_BEFORE_EXPIRY_MS = 60_000;
const MIN_REFRESH_DELAY_MS = 5_000;
const PUBLIC_PREFIXES = ['/login', '/register'];

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly authApi = inject(AuthApi);
  private readonly users = inject(UserService);
  private readonly router = inject(Router);
  private readonly isBrowser = isPlatformBrowser(inject(PLATFORM_ID));

  private readonly session = signal<Session | null>(null);
  private timer: ReturnType<typeof setTimeout> | null = null;

  
  private refresh$: Observable<string> | null = null;

  
  readonly accessToken = computed(() => this.session()?.accessToken ?? null);

  readonly isAuthenticated = computed(() => this.session() !== null);

  
  readonly ready: Promise<void>;

  constructor() {
    
    this.ready = this.isBrowser
      ? Promise.resolve().then(() => this.restore())
      : Promise.resolve();
  }

  login(request: LoginRequest, rememberMe = false): Observable<AuthResponse> {
    return this.authApi.login(request).pipe(tap((res) => this.activate(res, rememberMe)));
  }

  
  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.authApi.register(request).pipe(
      switchMap(() => this.login({ email: request.email, password: request.password })),
    );
  }

  logout(): void {
    const session = this.session();
    this.endSession();

    if (session) {
      this.authApi
        .logout(session.accessToken, session.refreshToken)
        .pipe(catchError(() => of(undefined)))
        .subscribe();
    }

    void this.router.navigate(['/login']);
  }

  refreshAccessToken(): Observable<string> {
    if (this.refresh$) return this.refresh$;

    const current = this.session();
    if (!current) return throwError(() => new Error('No active session.'));

    this.refresh$ = this.authApi.refresh({ refreshToken: current.refreshToken }).pipe(
      tap((res) => this.activate(res, current.rememberMe)),
      map((res) => res.accessToken),
      catchError((error: unknown) => {
        this.expireSession();
        return throwError(() => error);
      }),
      finalize(() => (this.refresh$ = null)),
      shareReplay(1),
    );

    return this.refresh$;
  }

  
  expireSession(): void {
    if (!this.session()) return;
    this.endSession();

    const current = this.router.url;
    if (PUBLIC_PREFIXES.some((p) => current.startsWith(p))) return;

    void this.router.navigate(['/login'], {
      queryParams: { reason: 'expired', returnUrl: current },
    });
  }

  
  private async restore(): Promise<void> {
    const stored = this.storage?.getItem(STORAGE_KEY);
    if (!stored) return;

    try {
      await firstValueFrom(
        this.authApi
          .refresh({ refreshToken: stored })
          .pipe(tap((res) => this.activate(res, true))),
      );
    } catch {
      this.removeStored();
    }
  }

  private activate(res: AuthResponse, rememberMe: boolean): void {
    this.clearTimer();

    const expiresAt = Date.now() + res.expiresInSeconds * 1000;

    this.session.set({
      accessToken: res.accessToken,
      refreshToken: res.refreshToken,
      expiresAt,
      rememberMe,
    });

    if (rememberMe) this.storage?.setItem(STORAGE_KEY, res.refreshToken);
    else this.removeStored();

    // Avatarul se încarcă doar când se schimbă utilizatorul, nu la fiecare refresh.
    const previousId = this.users.currentUser()?.id;
    this.users.setCurrentUser(res.user);
    if (previousId !== res.user.id) {
      this.users.loadAvatar(res.user.id).subscribe({ error: () => undefined });
    }

    this.scheduleRefresh(expiresAt);
  }

  private scheduleRefresh(expiresAt: number): void {
    const delay = Math.max(
      expiresAt - Date.now() - REFRESH_BEFORE_EXPIRY_MS,
      MIN_REFRESH_DELAY_MS,
    );

    this.timer = setTimeout(() => {
      this.refreshAccessToken().subscribe({ error: () => undefined });
    }, delay);
  }

  private endSession(): void {
    this.clearTimer();
    this.session.set(null);
    this.users.reset();
    this.removeStored();
  }

  private clearTimer(): void {
    if (this.timer !== null) {
      clearTimeout(this.timer);
      this.timer = null;
    }
  }


  private get storage(): Storage | null {
    return this.isBrowser ? window.localStorage : null;
  }

  private removeStored(): void {
    this.storage?.removeItem(STORAGE_KEY);
  }
}