import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
  Validators
} from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ReservationService } from '../../core/services/reservation.service';
import { Payment, PaymentMethod, Reservation } from '../../core/models/reservation.model';
import { nightsBetween, toInt } from '../../core/utils/date.utils';
import { extractErrorMessage } from '../../core/utils/error.utils';
import { statusLabel } from '../../core/utils/status.utils';
import { BookingSteps } from '../../shared/components/booking-steps/booking-steps';

// ---------- Demo kart doğrulamaları (sadece format — kart bilgisi hiçbir yere gönderilmez) ----------
const notBlank: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const value = control.value as string;
  return value && value.trim().length === 0 ? { blank: true } : null;
};

const cardNumber: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const digits = (control.value as string || '').replace(/\s/g, '');
  if (!digits) return null;
  return /^\d{16}$/.test(digits) ? null : { cardNumber: true };
};

const expiry: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const value = control.value as string;
  if (!value) return null;
  const match = /^(0[1-9]|1[0-2])\/(\d{2})$/.exec(value);
  if (!match) return { expiryFormat: true };

  const month = Number(match[1]);
  const year = 2000 + Number(match[2]);
  const lastDayOfMonth = new Date(year, month, 0, 23, 59, 59);
  return lastDayOfMonth < new Date() ? { expired: true } : null;
};

const cvv: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const value = control.value as string;
  if (!value) return null;
  return /^\d{3}$/.test(value) ? null : { cvv: true };
};

@Component({
  selector: 'app-payment',
  imports: [CurrencyPipe, DatePipe, ReactiveFormsModule, RouterLink, BookingSteps],
  templateUrl: './payment.html',
  styleUrl: './payment.css'
})
export class PaymentPage implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly reservationService = inject(ReservationService);

  // URL: /customer/payment/:reservationId
  readonly reservationId = input<string>();

  protected readonly reservation = signal<Reservation | null>(null);
  protected readonly loading = signal(true);
  protected readonly loadError = signal<string | null>(null);

  protected readonly method = signal<PaymentMethod>('card');
  protected readonly submitted = signal(false);
  protected readonly submitting = signal(false);
  protected readonly submitError = signal<string | null>(null);
  protected readonly completedPayment = signal<Payment | null>(null);

  protected readonly cardForm = this.fb.nonNullable.group({
    holder: ['', [Validators.required, notBlank, Validators.maxLength(60)]],
    number: ['', [Validators.required, cardNumber]],
    expiry: ['', [Validators.required, expiry]],
    cvv: ['', [Validators.required, cvv]]
  });

  protected readonly nights = computed(() => {
    const r = this.reservation();
    return r ? nightsBetween(r.checkInDate, r.checkOutDate) : 0;
  });

  // Zaten ödenmiş / iptal edilmiş rezervasyon tekrar ödenemez
  protected readonly notPayable = computed(() => {
    const r = this.reservation();
    return !!r && r.status !== 'pending' && !this.completedPayment();
  });

  protected readonly statusLabel = statusLabel;

  ngOnInit(): void {
    this.loadReservation();
  }

  loadReservation(): void {
    const id = toInt(this.reservationId(), 0);
    if (!id) {
      this.loadError.set('Rezervasyon bulunamadı.');
      this.loading.set(false);
      return;
    }

    this.loading.set(true);
    this.loadError.set(null);
    this.reservationService.getById(id).subscribe({
      next: reservation => {
        this.reservation.set(reservation);
        this.loading.set(false);
      },
      error: err => {
        console.error('Rezervasyon yüklenemedi:', err);
        this.loadError.set(extractErrorMessage(err, 'Rezervasyon bilgisi yüklenemedi.'));
        this.loading.set(false);
      }
    });
  }

  selectMethod(method: PaymentMethod): void {
    this.method.set(method);
    this.submitError.set(null);
  }

  // Kart numarasını 4'erli gruplar: 1234 5678 9012 3456
  formatCardNumber(event: Event): void {
    const input = event.target as HTMLInputElement;
    const digits = input.value.replace(/\D/g, '').slice(0, 16);
    const formatted = digits.replace(/(\d{4})(?=\d)/g, '$1 ');
    this.cardForm.controls.number.setValue(formatted);
  }

  // Son kullanma tarihine otomatik "/" ekler: 0828 → 08/28
  formatExpiry(event: Event): void {
    const input = event.target as HTMLInputElement;
    const digits = input.value.replace(/\D/g, '').slice(0, 4);
    const formatted = digits.length > 2 ? `${digits.slice(0, 2)}/${digits.slice(2)}` : digits;
    this.cardForm.controls.expiry.setValue(formatted);
  }

  onlyDigits(event: Event, max: number): void {
    const input = event.target as HTMLInputElement;
    this.cardForm.controls.cvv.setValue(input.value.replace(/\D/g, '').slice(0, max));
  }

  pay(): void {
    const reservation = this.reservation();
    this.submitted.set(true);
    this.submitError.set(null);

    if (!reservation || this.submitting()) return;
    if (this.method() === 'card' && this.cardForm.invalid) {
      this.cardForm.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    this.reservationService.pay(reservation.id, this.method()).subscribe({
      next: payment => {
        this.submitting.set(false);
        this.completedPayment.set(payment);
        this.reservation.set({ ...reservation, status: 'confirmed' });
        // Kart bilgilerini bellekten temizle
        this.cardForm.reset();
      },
      error: err => {
        this.submitting.set(false);
        this.submitError.set(extractErrorMessage(err, 'Ödeme alınamadı. Lütfen tekrar deneyin.'));
      }
    });
  }

  // ---------- Hata mesajları ----------
  cardError(name: 'holder' | 'number' | 'expiry' | 'cvv'): string | null {
    const control = this.cardForm.controls[name];
    if (!control.invalid || !(control.touched || this.submitted())) return null;
    const e = control.errors ?? {};

    if (e['required'] || e['blank']) {
      return {
        holder: 'Kart sahibinin adı zorunludur.',
        number: 'Kart numarası zorunludur.',
        expiry: 'Son kullanma tarihi zorunludur.',
        cvv: 'CVV zorunludur.'
      }[name];
    }
    if (e['cardNumber']) return 'Kart numarası 16 haneli olmalıdır.';
    if (e['expiryFormat']) return 'AA/YY formatında girin (ör. 08/28).';
    if (e['expired']) return 'Kartın son kullanma tarihi geçmiş.';
    if (e['cvv']) return 'CVV 3 haneli olmalıdır.';
    if (e['maxlength']) return 'Bu alan çok uzun.';
    return null;
  }
}