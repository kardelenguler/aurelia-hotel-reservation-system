import { Routes } from '@angular/router';
import { PublicLayout } from './layouts/public-layout/public-layout';
import { Home } from './features/home/home';
import { Search } from './features/search/search';
import { Login } from './features/auth/login/login';
import { Register } from './features/auth/register/register';
import { Booking } from './features/booking/booking';
import { PaymentPage } from './features/payment/payment';
import { authGuard, guestGuard } from './core/guards/auth.guards';

export const routes: Routes = [
  {
    path: '',
    component: PublicLayout,
    children: [
      { path: '', component: Home, title: 'Aurelia Grand Resort' },
      { path: 'search', component: Search, title: 'Müsait Odalar | Aurelia Grand Resort' },

      // Sadece giriş yapmamış kullanıcılar
      { path: 'login', component: Login, canActivate: [guestGuard], title: 'Giriş Yap | Aurelia Grand Resort' },
      { path: 'register', component: Register, canActivate: [guestGuard], title: 'Kayıt Ol | Aurelia Grand Resort' },

      // Sadece giriş yapmış kullanıcılar
      { path: 'customer/booking', component: Booking, canActivate: [authGuard], title: 'Rezervasyon | Aurelia Grand Resort' },
      { path: 'customer/payment/:reservationId', component: PaymentPage, canActivate: [authGuard], title: 'Ödeme | Aurelia Grand Resort' }
    ]
  },
  // Bilinmeyen adresler → ana sayfa
  { path: '**', redirectTo: '' }
];