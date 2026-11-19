import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from './api.config';
import { LoginRequest, AuthResponse, RefreshTokenRequest, LogoutRequest } from '../ui-models/auth.model';


@Injectable({ providedIn: 'root' })
export class AuthApi {
  private readonly http = inject(HttpClient);
  private readonly base = inject(API_BASE_URL);

  
  login(body: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.base}/auth/login`, body);
  }

  /** POST /api/auth/refresh (folosit în iterația următoare) */
  refresh(body: RefreshTokenRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.base}/auth/refresh`, body);
  }

  /** POST /api/auth/logout (folosit în iterația următoare) */
  logout(body: LogoutRequest): Observable<void> {
    return this.http.post<void>(`${this.base}/auth/logout`, body);
  }
}