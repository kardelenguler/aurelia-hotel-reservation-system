import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ReservationService } from '../../core/services/reservation.service';
import { CancellationPreview, Reservation } from '../../core/models/reservation.model';
import { nightsBetween, todayString } from '../../core/utils/date.utils';
import { extractErrorMessage } from '../../core/utils/error.utils';
import { hideBrokenImage, roomImageFor } from '../../core/utils/room-images';
import { ConfirmDialog } from '../../shared/components/confirm-dialog/confirm-dialog';
import { StatusBadge } from '../../shared/components/status-badge/status-badge';

type Filter = 'all' | 'upcoming' | 'past' | 'cancelled';

// Bir rezervasyon hangi filtreye uyuyor?
function matchesFilter(r: Reservation, filter: Filter): boolean {
  switch (filter) {
    case 'upcoming':
      return r.status === 'pending' || r.status === 'confirmed' || r.status === 'checked_in';
    case 'past':
      return r.status === 'checked_out';
    case 'cancelled':
      return r.status === 'cancelled';
    default:
      return true;
  }
}

@Component({
  selector: 'app-my-reservations',
  imports: [CurrencyPipe, DatePipe, RouterLink, ConfirmDialog, StatusBadge],
  templateUrl: './my-reservations.html',
  styleUrl: './my-reservations.css'
})
export class MyReservations implements OnInit {
  private readonly reservationService = inject(ReservationService);

  protected readonly reservations = signal<Reservation[]>([]);
  protected readonly loading = signal(true);
  protected readonly loadError = signal<string | null>(null);
  protected readonly filter = signal<Filter>('all');

  // İptal penceresi
  protected readonly cancelTarget = signal<Reservation | null>(null);
  protected readonly preview = signal<CancellationPreview | null>(null);
  protected readonly previewLoading = signal(false);
  protected readonly cancelling = signal(false);
  protected readonly cancelError = signal<string | null>(null);
  protected readonly lastCancelled = signal<{ id: number; refund: number } | null>(null);

  protected readonly filters: { key: Filter; label: string }[] = [
    { key: 'all', label: 'Tümü' },
    { key: 'upcoming', label: 'Yaklaşan' },
    { key: 'past', label: 'Geçmiş' },
    { key: 'cancelled', label: 'İptal Edilen' }
  ];

  // Seçili filtreye göre liste
  protected readonly filtered = computed(() =>
    this.reservations().filter(r => matchesFilter(r, this.filter()))
  );

  // Her filtrenin sayısı (sadece okur, hiçbir şeyi değiştirmez)
  protected readonly counts = computed<Record<Filter, number>>(() => {
    const list = this.reservations();
    return {
      all: list.length,
      upcoming: list.filter(r => matchesFilter(r, 'upcoming')).length,
      past: list.filter(r => matchesFilter(r, 'past')).length,
      cancelled: list.filter(r => matchesFilter(r, 'cancelled')).length
    };
  });

  protected readonly roomImage = (r: Reservation) => roomImageFor(r.room.roomType.name);
  protected readonly hideBrokenImage = hideBrokenImage;
  protected readonly nights = (r: Reservation) => nightsBetween(r.checkInDate, r.checkOutDate);

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.loadError.set(null);
    this.reservationService.getMyReservations().subscribe({
      next: list => {
        this.reservations.set(list);
        this.loading.set(false);
      },
      error: err => {
        console.error('Rezervasyonlar yüklenemedi:', err);
        this.loadError.set(extractErrorMessage(err, 'Rezervasyonlarınız yüklenemedi.'));
        this.loading.set(false);
      }
    });
  }

  setFilter(key: Filter): void {
    this.filter.set(key);
  }

  // İptal butonu sadece giriş tarihi gelmemiş, aktif rezervasyonlarda görünür
  canCancel(r: Reservation): boolean {
    return (r.status === 'pending' || r.status === 'confirmed') && r.checkInDate > todayString();
  }

  // ---------- İptal akışı ----------
  openCancel(r: Reservation): void {
    this.cancelTarget.set(r);
    this.preview.set(null);
    this.cancelError.set(null);
    this.previewLoading.set(true);

    this.reservationService.previewCancellation(r.id).subscribe({
      next: preview => {
        this.preview.set(preview);
        this.previewLoading.set(false);
      },
      error: err => {
        this.cancelError.set(extractErrorMessage(err, 'İade bilgisi alınamadı.'));
        this.previewLoading.set(false);
      }
    });
  }

  closeCancel(): void {
    this.cancelTarget.set(null);
    this.preview.set(null);
  }

  confirmCancel(): void {
    const target = this.cancelTarget();
    const preview = this.preview();
    if (!target || !preview?.cancellable || this.cancelling()) return;

    this.cancelling.set(true);
    this.cancelError.set(null);

    this.reservationService.cancel(target.id).subscribe({
      next: updated => {
        this.cancelling.set(false);
        this.reservations.update(list => list.map(r => (r.id === updated.id ? updated : r)));
        this.lastCancelled.set({ id: updated.id, refund: preview.refundAmount });
        this.closeCancel();
      },
      error: err => {
        this.cancelling.set(false);
        this.cancelError.set(extractErrorMessage(err, 'Rezervasyon iptal edilemedi. Lütfen tekrar deneyin.'));
      }
    });
  }
}