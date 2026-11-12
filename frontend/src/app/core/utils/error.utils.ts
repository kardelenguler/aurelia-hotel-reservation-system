import { HttpErrorResponse } from '@angular/common/http';

// Backend hatasını kullanıcıya gösterilecek anlaşılır bir mesaja çevirir
export function extractErrorMessage(error: unknown, fallback: string): string {
  if (error instanceof HttpErrorResponse) {
    // Sunucuya hiç ulaşılamadı (backend kapalı / internet yok)
    if (error.status === 0) {
      return 'Sunucuya ulaşılamıyor. Lütfen bağlantınızı kontrol edip tekrar deneyin.';
    }

    // Backend'in kendi mesajı (GlobalExceptionHandler'dan gelen "message")
    const message = error.error?.message;
    if (typeof message === 'string' && message.trim()) {
      return message;
    }

    // Teknik detayı göstermeden genel sunucu hatası
    if (error.status >= 500) {
      return 'Sunucuda beklenmeyen bir hata oluştu. Lütfen daha sonra tekrar deneyin.';
    }
  }
  return fallback;
}

// Giriş sonrası dönülecek adres güvenli mi? (sadece site içi adresler — başka siteye yönlendirme engellenir)
export function isSafeReturnUrl(url: string | null | undefined): url is string {
  return !!url && url.startsWith('/') && !url.startsWith('//');
}