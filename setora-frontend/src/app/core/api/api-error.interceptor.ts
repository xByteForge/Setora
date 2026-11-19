import { HttpErrorResponse } from '@angular/common/http';

export class ApiError extends Error {
  constructor(
    public readonly status: number,
    message: string,
    public readonly body?: unknown,
  ) {
    super(message);
    this.name = 'ApiError';
  }

  get isUnauthorized(): boolean {
    return this.status === 401;
  }

  get isNotImplemented(): boolean {
    return this.status === 501;
  }
}


export function toApiError(error: unknown): ApiError {
  if (error instanceof ApiError) return error;

  if (error instanceof HttpErrorResponse) {
    const backendMessage =
      typeof error.error?.message === 'string' ? error.error.message : null;
    return new ApiError(error.status, backendMessage ?? 'ERRORS.GENERIC', error.error);
  }

  return new ApiError(0, 'ERRORS.GENERIC', error);
}