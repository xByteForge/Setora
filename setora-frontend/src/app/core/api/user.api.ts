import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from './api.config';
import { User } from '../models/user.model';
import { UpdateUserRequest, ChangePasswordRequest } from '../ui-models/user-api.model';

@Injectable({ providedIn: 'root' })
export class UserApi {
  private readonly http = inject(HttpClient);
  private readonly base = inject(API_BASE_URL);

  getMe(): Observable<User> {
    return this.http.get<User>(`${this.base}/auth/me`);
  }

  updateMe(body: UpdateUserRequest): Observable<User> {
    return this.http.put<User>(`${this.base}/auth/me`, body);
  }

  changePassword(body: ChangePasswordRequest): Observable<void> {
    return this.http.put<void>(`${this.base}/auth/change-password`, body);
  }

  uploadAvatar(file: File): Observable<string> {
    const form = new FormData();
    form.append('file', file);
    return this.http.post(`${this.base}/avatar/upload`, form, { responseType: 'text' });
  }

  getAvatar(userId: number): Observable<Blob> {
    return this.http.get(`${this.base}/avatar/${userId}`, { responseType: 'blob' });
  }
}
