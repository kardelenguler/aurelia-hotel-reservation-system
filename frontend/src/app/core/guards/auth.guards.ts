import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

// Giriş yapmış kullanıcılar (müşteri veya personel)
export const authGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (auth.token) {
    return true;
  }
  // Giriş sonrası kaldığı yere dönsün
  return router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
};

// Sadece otel personeli (resepsiyon + yönetici)
export const staffGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (!auth.token) {
    return router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
  }
  return auth.isStaff() ? true : router.createUrlTree(['/']);
};

// Giriş/kayıt sayfaları: zaten giriş yapmışsa tekrar gösterme
export const guestGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (!auth.token) {
    return true;
  }
  return router.createUrlTree([auth.isStaff() ? '/admin/dashboard' : '/']);
};