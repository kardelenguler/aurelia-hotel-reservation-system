import { Component, input } from '@angular/core';

@Component({
  selector: 'app-booking',
  template: `
    <section class="container section">
      <div class="section-header">
        <span class="eyebrow">Rezervasyon</span>
        <h2>Rezervasyon Oluştur</h2>
      </div>
      <div class="card" style="max-width: 520px; margin-inline: auto; padding: 32px;">
        <p><strong>Oda id:</strong> {{ roomId() }}</p>
        <p><strong>Giriş:</strong> {{ checkIn() }}</p>
        <p><strong>Çıkış:</strong> {{ checkOut() }}</p>
        <p><strong>Kişi:</strong> {{ adults() }} yetişkin, {{ children() }} çocuk</p>
        <p style="margin-top: 16px; color: var(--color-muted);">
          Bu sayfa bir sonraki adımda tamamlanacak. ✅ Giriş yönlendirmesi çalışıyor.
        </p>
      </div>
    </section>
  `
})
export class Booking {
  readonly roomId = input<string>();
  readonly checkIn = input<string>();
  readonly checkOut = input<string>();
  readonly adults = input<string>();
  readonly children = input<string>();
}