import { isPlatformBrowser } from '@angular/common';
import { computed, inject, Injectable, PLATFORM_ID, signal } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, Observable, of, switchMap, tap } from 'rxjs';
import { AuthApi } from '../api/auth.api';
import { User } from '../models/user.model';
import { AuthResponse, LoginRequest, RegisterRequest } from '../ui-models/auth.model';
import { UserService } from './user.service';

interface Session {
  accessToken: string;
  refreshToken: string;
  /** Momentul expirării, în milisecunde de la epoch. */
  expiresAt: number;
  user: User;
}

const STORAGE_KEY = 'setora_session';
const PUBLIC_PREFIXES = ['/login', '/register'];

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly authApi = inject(AuthApi);
  private readonly users = inject(UserService);
  private readonly router = inject(Router);
  private readonly isBrowser = isPlatformBrowser(inject(PLATFORM_ID));

  private readonly session = signal<Session | null>(null);
  private expiryTimer: ReturnType<typeof setTimeout> | null = null;

  
  readonly accessToken = computed(() => this.session()?.accessToken ?? null);

  
  readonly isAuthenticated = computed(() => {
    const s = this.session();
    return s !== null && Date.now() < s.expiresAt;
  });

  constructor() {
    
    const stored = this.readStored();
    if (stored) this.activate(stored, true);
  }

  
  login(request: LoginRequest, rememberMe = false): Observable<AuthResponse> {
    return this.authApi.login(request).pipe(
      tap((res) => this.activate(this.toSession(res), rememberMe)),
    );
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

  
  handleUnauthorized(): void {
    const session = this.session();
    if (!session) return;

    if (Date.now() < session.expiresAt) return;

    this.expireSession();
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

  private toSession(res: AuthResponse): Session {
    return {
      accessToken: res.accessToken,
      refreshToken: res.refreshToken,
      expiresAt: Date.now() + res.expiresInSeconds * 1000,
      user: res.user,
    };
  }

  private activate(session: Session, remember: boolean): void {
    this.clearTimer();
    this.session.set(session);

    this.users.setCurrentUser(session.user);
    this.users.loadAvatar(session.user.id).subscribe({ error: () => undefined });

    if (remember) this.writeStored(session);
    else this.removeStored();

    this.expiryTimer = setTimeout(
      () => this.expireSession(),
      Math.max(session.expiresAt - Date.now(), 0),
    );
  }

  private endSession(): void {
    this.clearTimer();
    this.session.set(null);
    this.users.reset();
    this.removeStored();
  }

  private clearTimer(): void {
    if (this.expiryTimer !== null) {
      clearTimeout(this.expiryTimer);
      this.expiryTimer = null;
    }
  }

  

  private get storage(): Storage | null {
    return this.isBrowser ? window.localStorage : null;
  }

  private readStored(): Session | null {
    const raw = this.storage?.getItem(STORAGE_KEY);
    if (!raw) return null;

    try {
      const session = JSON.parse(raw) as Session;
      if (session.accessToken && session.expiresAt > Date.now()) return session;
    } catch {
      // 
    }

    this.removeStored();
    return null;
  }

  private writeStored(session: Session): void {
    this.storage?.setItem(STORAGE_KEY, JSON.stringify(session));
  }

  private removeStored(): void {
    this.storage?.removeItem(STORAGE_KEY);
  }
}