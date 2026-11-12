import { Component, input, output } from '@angular/core';

// Ortak onay penceresi (iptal, pasife alma, check-in/out...)
@Component({
  selector: 'app-confirm-dialog',
  host: { '(document:keydown.escape)': 'onEscape()' },
  template: `
    @if (open()) {
      <div class="backdrop" (click)="onEscape()"></div>
      <div class="dialog" role="dialog" aria-modal="true" [attr.aria-label]="title()">
        <h3>{{ title() }}</h3>
        <div class="body"><ng-content /></div>
        <div class="actions">
          <button type="button" class="btn btn-outline btn-sm" [disabled]="loading()" (click)="cancelled.emit()">
            {{ cancelLabel() }}
          </button>
          <button type="button" class="btn btn-sm" [class.btn-danger]="danger()" [class.btn-primary]="!danger()"
                  [disabled]="loading() || confirmDisabled()" (click)="confirmed.emit()">
            @if (loading()) { İşleniyor... } @else { {{ confirmLabel() }} }
          </button>
        </div>
      </div>
    }
  `,
  styles: `
    .backdrop {
      position: fixed;
      inset: 0;
      z-index: 200;
      background: rgba(36, 42, 43, 0.55);
    }
    .dialog {
      position: fixed;
      z-index: 201;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      width: min(460px, calc(100vw - 32px));
      padding: 28px;
      border-radius: var(--radius-card);
      background: var(--color-white);
      box-shadow: var(--shadow-card-hover);
    }
    .body {
      margin-top: 12px;
      color: var(--color-charcoal);
    }
    .actions {
      display: flex;
      justify-content: flex-end;
      gap: 12px;
      margin-top: 24px;
    }
    .btn-danger {
      background: var(--color-error);
      color: var(--color-white);
    }
    .btn-danger:hover:not(:disabled) {
      background: #8f1e18;
    }
  `
})
export class ConfirmDialog {
  readonly open = input(false);
  readonly title = input('Emin misiniz?');
  readonly confirmLabel = input('Onayla');
  readonly cancelLabel = input('Vazgeç');
  readonly danger = input(false);
  readonly loading = input(false);
  readonly confirmDisabled = input(false);

  readonly confirmed = output<void>();
  readonly cancelled = output<void>();

  // ESC veya arka plana tıklayınca kapat (işlem sürerken kapanmasın)
  onEscape(): void {
    if (this.open() && !this.loading()) {
      this.cancelled.emit();
    }
  }
}