import { Component, input } from '@angular/core';

// Rezervasyon adımları göstergesi: 1 Oda Seçimi → 2 Özet → 3 Ödeme
@Component({
  selector: 'app-booking-steps',
  template: `
    <ol class="steps" aria-label="Rezervasyon adımları">
      @for (step of steps; track step; let i = $index) {
        <li
          class="step"
          [class.done]="i + 1 < current()"
          [class.active]="i + 1 === current()"
          [attr.aria-current]="i + 1 === current() ? 'step' : null">
          <span class="step-number">
            @if (i + 1 < current()) { ✓ } @else { {{ i + 1 }} }
          </span>
          <span class="step-label">{{ step }}</span>
        </li>
      }
    </ol>
  `,
  styles: `
    .steps {
      display: flex;
      justify-content: center;
      gap: 12px;
      list-style: none;
      margin: 0 0 40px;
      padding: 0;
    }
    .step {
      display: flex;
      align-items: center;
      gap: 10px;
      color: var(--color-muted);
      font-size: 0.9rem;
      font-weight: 500;
    }
    .step:not(:last-child)::after {
      content: '';
      width: 48px;
      height: 1px;
      margin-left: 2px;
      background: var(--color-border);
    }
    .step-number {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      width: 32px;
      height: 32px;
      border-radius: 50%;
      border: 1px solid var(--color-border);
      background: var(--color-white);
    }
    .step.active { color: var(--color-ocean); }
    .step.active .step-number {
      background: var(--color-ocean);
      border-color: var(--color-ocean);
      color: var(--color-white);
    }
    .step.done { color: var(--color-gold-dark); }
    .step.done .step-number {
      background: var(--color-gold);
      border-color: var(--color-gold);
      color: var(--color-white);
    }
    @media (max-width: 640px) {
      .step-label { display: none; }
      .step:not(:last-child)::after { width: 24px; }
    }
  `
})
export class BookingSteps {
  readonly current = input(1);
  protected readonly steps = ['Oda Seçimi', 'Rezervasyon Özeti', 'Ödeme'];
}