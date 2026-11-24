import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { API_BASE_URL } from './api.config';

import { AuthService } from '../services/auth.service';
import { toApiError } from './api-error.interceptor';

const PUBLIC_PATHS = new Set(['/auth/login', '/auth/register', '/auth/refresh']);

function relativeApiPath(requestUrl: string, apiBaseUrl: string): string | null {
  let api: URL;
  let target: URL;

  try {
    api = new URL(apiBaseUrl);
    target = new URL(requestUrl);
  } catch {
    return null;
  }

  if (target.origin !== api.origin) return null;

  const basePath = api.pathname.replace(/\/$/, '');
  const pathname = target.pathname.replace(/\/$/, '');

  if (pathname !== basePath && !pathname.startsWith(`${basePath}/`)) return null;

  return pathname.slice(basePath.length) || '/';
}

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const relativePath = relativeApiPath(req.url, inject(API_BASE_URL));

  if (relativePath === null) return next(req);

  if (PUBLIC_PATHS.has(relativePath)) return next(req);

  const token = inject(AuthService).accessToken();
  if (!token) return next(req);

  return next(req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }));
};

export const apiErrorInterceptor: HttpInterceptorFn = (req, next) =>
  next(req).pipe(catchError((error: unknown) => throwError(() => toApiError(error))));
