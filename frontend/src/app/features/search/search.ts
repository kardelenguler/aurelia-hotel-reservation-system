import { Component, DestroyRef, computed, effect, inject, input, signal, untracked } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import { RoomService } from '../../core/services/room.service';
import { Room, RoomType } from '../../core/models/room.model';
import { MAX_GUESTS, nightsBetween, toInt, todayString } from '../../core/utils/date.utils';
import { hideBrokenImage, roomImageFor } from '../../core/utils/room-images';
import { SearchBox } from '../../shared/components/search-box/search-box';

// Aynı tipteki müsait odalar tek kartta gösterilir
interface RoomTypeGroup {
  type: RoomType;
  rooms: Room[];
}

@Component({
  selector: 'app-search',
  imports: [CurrencyPipe, DatePipe, RouterLink, SearchBox],
  templateUrl: './search.html',
  styleUrl: './search.css'
})
export class Search {
  private readonly roomService = inject(RoomService);
  private readonly router = inject(Router);
  private currentRequest?: Subscription;

  // URL'deki ?checkIn=...&checkOut=...&adults=...&children=... otomatik buraya gelir
  readonly checkIn = input<string>();
  readonly checkOut = input<string>();
  readonly adults = input<string>();
  readonly children = input<string>();

  protected readonly adultCount = computed(() => toInt(this.adults(), 0));
  protected readonly childCount = computed(() => toInt(this.children(), 0));
  protected readonly guestCount = computed(() => this.adultCount() + this.childCount());

  // URL bilgileri geçersizse kullanıcıya gösterilecek mesaj
  protected readonly paramError = computed<string | null>(() => {
    const checkIn = this.checkIn();
    const checkOut = this.checkOut();
    if (!checkIn || !checkOut) return 'Müsait odaları görmek için giriş ve çıkış tarihi seçin.';
    if (checkIn < todayString()) return 'Giriş tarihi geçmiş bir tarih olamaz.';
    if (checkOut <= checkIn) return 'Çıkış tarihi giriş tarihinden sonra olmalıdır.';
    if (this.adultCount() < 1 || this.childCount() < 0) return 'Kişi sayısı geçersiz.';
    if (this.guestCount() > MAX_GUESTS) return `Bir odada en fazla ${MAX_GUESTS} kişi konaklayabilir.`;
    return null;
  });

  protected readonly nights = computed(() =>
    this.paramError() ? 0 : nightsBetween(this.checkIn()!, this.checkOut()!)
  );

  protected readonly rooms = signal<Room[]>([]);
  protected readonly loading = signal(false);
  protected readonly loadError = signal(false);
  protected readonly sortOrder = signal<'asc' | 'desc'>('asc');

  // Odaları tipe göre grupla, fiyata göre sırala
  protected readonly groups = computed<RoomTypeGroup[]>(() => {
    const map = new Map<number, RoomTypeGroup>();
    for (const room of this.rooms()) {
      const group = map.get(room.roomType.id);
      if (group) {
        group.rooms.push(room);
      } else {
        map.set(room.roomType.id, { type: room.roomType, rooms: [room] });
      }
    }
    const direction = this.sortOrder() === 'asc' ? 1 : -1;
    return [...map.values()].sort(
      (a, b) => (a.type.pricePerNight - b.type.pricePerNight) * direction
    );
  });

  protected readonly roomImage = (type: RoomType) => roomImageFor(type.name);
  protected readonly hideBrokenImage = hideBrokenImage;

  constructor() {
    // URL değişince (yeni arama) sonuçları yeniden yükle
    effect(() => {
      const error = this.paramError();
      const checkIn = this.checkIn();
      const checkOut = this.checkOut();
      const guests = this.guestCount();

      untracked(() => {
        if (error) {
          this.currentRequest?.unsubscribe();
          this.rooms.set([]);
          this.loading.set(false);
          return;
        }
        this.load(checkIn!, checkOut!, guests);
      });
    });

    // Sayfadan çıkılınca devam eden isteği iptal et
    inject(DestroyRef).onDestroy(() => this.currentRequest?.unsubscribe());
  }

  retry(): void {
    if (!this.paramError()) {
      this.load(this.checkIn()!, this.checkOut()!, this.guestCount());
    }
  }

  changeSort(event: Event): void {
    this.sortOrder.set((event.target as HTMLSelectElement).value as 'asc' | 'desc');
  }

  totalPrice(group: RoomTypeGroup): number {
    return group.type.pricePerNight * this.nights();
  }

  // Seçilen tipten ilk müsait odayı al → rezervasyon sayfasına geç
  selectRoom(group: RoomTypeGroup): void {
    const room = group.rooms[0];
    this.router.navigate(['/customer/booking'], {
      queryParams: {
        roomId: room.id,
        checkIn: this.checkIn(),
        checkOut: this.checkOut(),
        adults: this.adultCount(),
        children: this.childCount()
      }
    });
  }

  private load(checkIn: string, checkOut: string, guests: number): void {
    // Önceki arama hâlâ sürüyorsa iptal et (eski sonuç yenisinin üstüne yazmasın)
    this.currentRequest?.unsubscribe();
    this.loading.set(true);
    this.loadError.set(false);

    this.currentRequest = this.roomService.searchAvailable(checkIn, checkOut, guests).subscribe({
      next: rooms => {
        this.rooms.set(rooms);
        this.loading.set(false);
      },
      error: err => {
        console.error('Müsait odalar yüklenemedi:', err);
        this.rooms.set([]);
        this.loadError.set(true);
        this.loading.set(false);
      }
    });
  }
}