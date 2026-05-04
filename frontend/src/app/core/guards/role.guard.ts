import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { catchError, map, of } from 'rxjs';

import { UserRole } from '../models/user.model';
import { AuthService } from '../services/auth.service';

export const contributorGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  return requireRoles(auth, router, state.url, ['CONTRIBUTOR', 'ADMIN']);
};

export const adminGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  return requireRoles(auth, router, state.url, ['ADMIN']);
};

function requireRoles(auth: AuthService, router: Router, returnUrl: string, roles: readonly UserRole[]) {
  const loginUrl = router.createUrlTree(['/login'], {
    queryParams: {
      returnUrl
    }
  });

  if (!auth.isLoggedIn()) {
    return loginUrl;
  }

  return auth.refreshCurrentUser().pipe(
    map(() => (auth.hasAnyRole(roles) ? true : router.createUrlTree(['/']))),
    catchError(() => {
      auth.logout();
      return of(loginUrl);
    })
  );
}
