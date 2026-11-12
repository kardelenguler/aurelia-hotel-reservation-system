import { Routes } from '@angular/router';
import { PublicLayout } from './layouts/public-layout/public-layout';
import { Home } from './features/home/home';
import { Search } from './features/search/search';

export const routes: Routes = [
  {
    path: '',
    component: PublicLayout,
    children: [
      { path: '', component: Home, title: 'Aurelia Grand Resort' },
      { path: 'search', component: Search, title: 'Müsait Odalar | Aurelia Grand Resort' }
      // Diğer sayfalar sonraki adımlarda eklenecek
    ]
  },
  // Bilinmeyen adresler → ana sayfa
  { path: '**', redirectTo: '' }
];