import { Routes } from '@angular/router';
import { PublicLayout } from './layouts/public-layout/public-layout';
import { Home } from './features/home/home';

export const routes: Routes = [
  {
    path: '',
    component: PublicLayout,
    children: [
      { path: '', component: Home, title: 'Aurelia Grand Resort' }
      // Diğer sayfalar (rooms, login, register...) sonraki adımlarda eklenecek
    ]
  },
  // Bilinmeyen adresler → ana sayfa
  { path: '**', redirectTo: '' }
];