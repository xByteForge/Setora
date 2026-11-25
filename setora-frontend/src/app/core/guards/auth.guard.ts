import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { UserService } from '../services/user.service';
import { UserRole } from '../models/enums/user-role.model';

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

export const roleGuard: CanActivateFn = async (route) => {
  const auth = inject(AuthService);
  const users = inject(UserService);
  const router = inject(Router);

  await auth.ready;
  if (!auth.isAuthenticated()) return router.createUrlTree(['/login']);

  const allowed = (route.data['roles'] as UserRole[] | undefined) ?? [];
  const role = users.role();
  if (role && allowed.includes(role)) return true;

  return router.createUrlTree(['/discover']);
};