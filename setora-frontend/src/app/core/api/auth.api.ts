import { HttpClient, HttpHeaders } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from './api.config';
import { User } from '../models/user.model';
import {
  AuthResponse,
  LoginRequest,
  RefreshTokenRequest,
  RegisterRequest,
} from '../ui-models/auth.model';

@Injectable({ providedIn: 'root' })
export class AuthApi {
  private readonly http = inject(HttpClient);
  private readonly base = inject(API_BASE_URL);

  // POST /api/auth/login 
  login(body: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.base}/auth/login`, body);
  }

  // POST /api/auth/register. 
  register(body: RegisterRequest): Observable<User> {
    return this.http.post<User>(`${this.base}/auth/register`, body);
  }

  // POST /api/auth/refresh. 
  refresh(body: RefreshTokenRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.base}/auth/refresh`, body);
  }

  // POST /api/auth/logout. 
  logout(accessToken: string, refreshToken: string): Observable<void> {
    return this.http.post<void>(
      `${this.base}/auth/logout`,
      { refreshToken },
      { headers: new HttpHeaders({ Authorization: `Bearer ${accessToken}` }) },
    );
  }
}