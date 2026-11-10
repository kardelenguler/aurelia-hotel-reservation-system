import { Component } from '@angular/core';

@Component({
  selector: 'app-home',
  template: `
    <section class="container section">
      <div class="section-header">
        <span class="eyebrow">Aurelia Grand Resort</span>
        <h2>Ana Sayfa</h2>
      </div>
      <p style="text-align:center; color: var(--color-muted);">
        Hero, arama kutusu ve oda tipleri bir sonraki adımda buraya gelecek.
      </p>
    </section>
  `
})
export class Home {}