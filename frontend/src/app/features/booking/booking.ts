import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Router, RouterLink } from '@angular/router';
import { RoomService } from '../../core/services/room.service';
import { ReservationService } from '../../core/services/reservation.service';
import { Room } from '../../core/models/room.model';
import { MAX_GUESTS, nightsBetween, toInt, todayString } from '../../core/utils/date.utils';
import { extractErrorMessage } from '../../core/utils/error.utils';
import { hideBrokenImage, roomImageFor } from '../../core/utils/room-images';
import { BookingSteps } from '../../shared/components/booking-steps/booking-steps';

@Component({
  selector: 'app-booking',
  imports: [CurrencyPipe, DatePipe, RouterLink, BookingSteps],
  templateUrl: './booking.html',
  styleUrl: './booking.css'
})
export class Booking implements OnInit {
  private readonly roomService = inject(RoomService);
  private readonly reservationService = inject(ReservationService);
  private readonly router = inject(Router);

  // URL parametreleri
  readonly roomId = input<string>();
  readonly checkIn = input<string>();
  readonly checkOut = input<string>();
  readonly adults = input<string>();
  readonly children = input<string>();

  protected readonly adultCount = computed(() => toInt(this.adults(), 0));
  protected readonly childCount = computed(() => toInt(this.children(), 0));
  protected readonly guestCount = computed(() => this.adultCount() + this.childCount());

  protected readonly room = signal<Room | null>(null);
  protected readonly loading = signal(true);
  protected readonly loadError = signal<string | null>(null);

  protected readonly termsAccepted = signal(false);
  protected readonly submitted = signal(false);
  protected readonly submitting = signal(false);
  protected readonly submitError = signal<string | null>(null);
  protected readonly roomTaken = signal(false);

  // URL bilgileri geçerli mi?
  protected readonly paramError = computed<string | null>(() => {
    const checkIn = this.checkIn();
    const checkOut = this.checkOut();
    if (!toInt(this.roomId(), 0)) return 'Oda bilgisi eksik.';
    if (!checkIn || !checkOut) return 'Tarih bilgisi eksik.';
    if (checkIn < todayString()) return 'Giriş tarihi geçmiş bir tarih olamaz.';
    if (checkOut <= checkIn) return 'Çıkış tarihi giriş tarihinden sonra olmalıdır.';
    if (this.adultCount() < 1 || this.childCount() < 0) return 'Kişi sayısı geçersiz.';
    if (this.guestCount() > MAX_GUESTS) return `Bir odada en fazla ${MAX_GUESTS} kişi konaklayabilir.`;
    return null;
  });

  protected readonly nights = computed(() =>
    this.paramError() ? 0 : nightsBetween(this.checkIn()!, this.checkOut()!)
  );

  // Ön izleme fiyatı (kesin tutarı backend hesaplar)
  protected readonly totalPrice = computed(() => {
    const room = this.room();
    return room ? room.roomType.pricePerNight * this.nights() : 0;
  });

  // Oda bu kişi sayısına uygun mu?
  protected readonly capacityError = computed<string | null>(() => {
    const room = this.room();
    if (!room) return null;
    if (!room.isActive) return 'Bu oda şu anda rezervasyona kapalı.';
    if (this.guestCount() > room.roomType.capacity) {
      return `Bu oda en fazla ${room.roomType.capacity} kişiliktir.`;
    }
    return null;
  });

  protected readonly roomImage = computed(() => {
    const room = this.room();
    return room ? roomImageFor(room.roomType.name) : '';
  });
  protected readonly hideBrokenImage = hideBrokenImage;

  ngOnInit(): void {
    if (!this.paramError()) {
      this.loadRoom();
    } else {
      this.loading.set(false);
    }
  }

  loadRoom(): void {
    this.loading.set(true);
    this.loadError.set(null);

    this.roomService.getRoom(toInt(this.roomId(), 0)).subscribe({
      next: room => {
        this.room.set(room);
        this.loading.set(false);
      },
      error: err => {
        console.error('Oda bilgisi yüklenemedi:', err);
        this.loadError.set(extractErrorMessage(err, 'Oda bilgisi yüklenemedi.'));
        this.loading.set(false);
      }
    });
  }

  toggleTerms(event: Event): void {
    this.termsAccepted.set((event.target as HTMLInputElement).checked);
  }

  confirm(): void {
    this.submitted.set(true);
    this.submitError.set(null);
    this.roomTaken.set(false);

    if (!this.termsAccepted() || this.capacityError() || this.submitting()) {
      return;
    }

    this.submitting.set(true);
    this.reservationService.create({
      roomId: toInt(this.roomId(), 0),
      checkInDate: this.checkIn()!,
      checkOutDate: this.checkOut()!,
      guestCount: this.guestCount()
    }).subscribe({
      next: reservation => {
        this.submitting.set(false);
        this.router.navigate(['/customer/payment', reservation.id]);
      },
      error: err => {
        this.submitting.set(false);
        // Başkası bu odayı az önce aldıysa
        if (err instanceof HttpErrorResponse && err.status === 409) {
          this.roomTaken.set(true);
        }
        this.submitError.set(extractErrorMessage(err, 'Rezervasyon oluşturulamadı. Lütfen tekrar deneyin.'));
      }
    });
  }

  // Aramaya aynı bilgilerle geri dön
  protected searchParams() {
    return {
      checkIn: this.checkIn(),
      checkOut: this.checkOut(),
      adults: this.adultCount(),
      children: this.childCount()
    };
  }
}