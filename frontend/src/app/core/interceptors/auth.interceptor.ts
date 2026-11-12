import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { API_URL } from '../api.config';
import { AuthService } from '../services/auth.service';

// Her API isteğine token'ı otomatik ekler; token geçersizse oturumu kapatır
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  // Token sadece bizim backend'e gider, başka sitelere asla
  const token = req.url.startsWith(API_URL) ? auth.token : null;
  const authReq = token
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      const isAuthRequest = req.url.includes('/auth/login') || req.url.includes('/auth/register');

      // Token gönderildi ama backend reddetti → süresi dolmuş/geçersiz
      if (error.status === 401 && token && !isAuthRequest) {
        auth.logout();
        router.navigate(['/login'], {
          queryParams: { sessionExpired: 1, returnUrl: router.url }
        });
      }
      return throwError(() => error);
    })
  );
};