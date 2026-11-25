import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { UserService } from '../services/user.service';

export const authGuard: CanActivateFn = async (_route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  await auth.ready;
  if (auth.isAuthenticated()) return true;

  return router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
};

export const guestGuard: CanActivateFn = async () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  await auth.ready;
  if (!auth.isAuthenticated()) return true;

  return router.createUrlTree(['/']);
};

export const ownerGuard: CanActivateFn = async () => {
  const auth = inject(AuthService);
  const users = inject(UserService);
  const router = inject(Router);

  await auth.ready;
  if (!auth.isAuthenticated()) return router.createUrlTree(['/login']);
  if (users.isOwner()) return true;

  return router.createUrlTree(['/discover']);
};