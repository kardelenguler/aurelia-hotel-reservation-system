import { Component, computed, input } from '@angular/core';
import { ReservationStatus } from '../../../core/models/reservation.model';
import { statusLabel } from '../../../core/utils/status.utils';

// Rezervasyon durumu etiketi
@Component({
  selector: 'app-status-badge',
  template: `<span class="badge" [class]="'badge ' + status()">{{ label() }}</span>`,
  styles: `
    .badge {
      display: inline-block;
      padding: 4px 12px;
      border-radius: 999px;
      font-size: 0.8rem;
      font-weight: 600;
      white-space: nowrap;
    }
    .pending { background: #FFF4E0; color: #9A6200; }
    .confirmed { background: var(--color-success-bg); color: var(--color-success); }
    .checked_in { background: #E3EEF1; color: var(--color-ocean); }
    .checked_out { background: #EEECE8; color: var(--color-muted); }
    .cancelled { background: var(--color-error-bg); color: var(--color-error); }
  `
})
export class StatusBadge {
  readonly status = input.required<ReservationStatus>();
  protected readonly label = computed(() => statusLabel(this.status()));
}