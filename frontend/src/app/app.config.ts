import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter, withComponentInputBinding, withInMemoryScrolling } from '@angular/router';
import { provideHttpClient, withFetch } from '@angular/common/http';

import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    // Yakalanmayan hataları konsola düzgün yazar
    provideBrowserGlobalErrorListeners(),

    // Sayfa yönlendirme
    provideRouter(
      routes,
      withComponentInputBinding(),                              // URL parametrelerini bileşene otomatik aktarır
      withInMemoryScrolling({ scrollPositionRestoration: 'top' }) // sayfa değişince en üste kaydır
    ),

    // Backend'e HTTP istekleri
    provideHttpClient(withFetch())
  ]
};