import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { API_BASE_URL } from './api.config';
import { toApiError } from './api-error.interceptor';
import { AuthService } from '../services/auth.service';


const PUBLIC_PATHS = ['/auth/login', '/auth/register', '/auth/refresh'];

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const apiBase = inject(API_BASE_URL);

  
  if (!req.url.startsWith(apiBase)) return next(req);
  if (PUBLIC_PATHS.some((path) => req.url.startsWith(apiBase + path))) return next(req);

  const token = inject(AuthService).accessToken();
  if (!token) return next(req);

  return next(req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }));
};

export const apiErrorInterceptor: HttpInterceptorFn = (req, next) =>
  next(req).pipe(
    catchError((error: unknown) => throwError(() => toApiError(error))),
  );