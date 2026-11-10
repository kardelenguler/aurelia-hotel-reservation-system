import { Component, OnInit, inject, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
  Validators
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { RoomService } from '../../core/services/room.service';
import { RoomType } from '../../core/models/room.model';

// Otelin en büyük odası (Family Suite) 5 kişilik
const MAX_GUESTS = 5;

// Bugünün tarihi (yerel saat, YYYY-AA-GG) — toISOString UTC kaydırması yapmasın diye elle
function todayString(): string {
  const d = new Date();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${d.getFullYear()}-${month}-${day}`;
}

// Geçmiş tarih seçilemez
const notInPast: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const value = control.value as string;
  return value && value < todayString() ? { pastDate: true } : null;
};

// Form geneli kurallar: çıkış > giriş, toplam kişi ≤ 5
const searchRules: ValidatorFn = (group: AbstractControl): ValidationErrors | null => {
  const checkIn = group.get('checkIn')?.value as string;
  const checkOut = group.get('checkOut')?.value as string;
  const adults = Number(group.get('adults')?.value) || 0;
  const children = Number(group.get('children')?.value) || 0;

  const errors: ValidationErrors = {};
  if (checkIn && checkOut && checkOut <= checkIn) {
    errors['dateOrder'] = true;
  }
  if (adults + children > MAX_GUESTS) {
    errors['tooManyGuests'] = true;
  }
  return Object.keys(errors).length ? errors : null;
};

// Oda tipi adına göre görsel (frontend/public/images içinde)
const ROOM_IMAGES: Record<string, string> = {
  'Standard Room': '/images/aurelia-room-standard.jpg',
  'Deluxe Sea View': '/images/aurelia-room-deluxe.jpg',
  'Family Suite': '/images/aurelia-room-family.jpg',
  'King Suite': '/images/aurelia-room-king.jpg'
};

@Component({
  selector: 'app-home',
  imports: [ReactiveFormsModule, RouterLink, CurrencyPipe],
  templateUrl: './home.html',
  styleUrl: './home.css'
})
export class Home implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly roomService = inject(RoomService);

  protected readonly today = todayString();
  protected readonly maxGuests = MAX_GUESTS;

  // ---------- Arama formu ----------
  protected readonly submitted = signal(false);

  protected readonly searchForm = this.fb.nonNullable.group(
    {
      checkIn: ['', [Validators.required, notInPast]],
      checkOut: ['', [Validators.required]],
      adults: [2, [Validators.required, Validators.min(1), Validators.max(10)]],
      children: [0, [Validators.required, Validators.min(0), Validators.max(10)]]
    },
    { validators: searchRules }
  );

  // ---------- Oda tipleri ----------
  protected readonly roomTypes = signal<RoomType[]>([]);
  protected readonly loading = signal(true);
  protected readonly loadError = signal(false);

  protected readonly highlights = [
    { icon: 'dining', title: 'Her Şey Dahil', text: 'Yeme, içme ve eğlence tek pakette' },
    { icon: 'lotus', title: 'Lüks Konaklama', text: 'Konforlu ve şık odalar' },
    { icon: 'waves', title: 'Deniz & Havuz', text: 'Özel plaj ve geniş havuzlar' },
    { icon: 'spa', title: 'Spa & Wellness', text: 'Ruhunuzu ve bedeninizi yenileyin' }
  ];

  protected readonly galleryImages = [
    { src: '/images/aurelia-gallery-1.jpg', alt: 'Palmiyelerle çevrili sonsuzluk havuzu' },
    { src: '/images/aurelia-gallery-2.jpg', alt: 'Deniz manzaralı açık hava restoranı' },
    { src: '/images/aurelia-gallery-3.jpg', alt: 'Özel plaj, şezlonglar ve iskele' },
    { src: '/images/aurelia-gallery-4.jpg', alt: 'Spa ve kapalı havuz' }
  ];

  ngOnInit(): void {
    this.loadRoomTypes();
  }

  loadRoomTypes(): void {
    this.loading.set(true);
    this.loadError.set(false);

    this.roomService.getRoomTypes().subscribe({
      next: types => {
        this.roomTypes.set(types);
        this.loading.set(false);
      },
      error: err => {
        console.error('Oda tipleri yüklenemedi:', err);
        this.loadError.set(true);
        this.loading.set(false);
      }
    });
  }

  search(): void {
    this.submitted.set(true);
    if (this.searchForm.invalid) {
      this.searchForm.markAllAsTouched();
      return;
    }

    const { checkIn, checkOut, adults, children } = this.searchForm.getRawValue();
    this.router.navigate(['/search'], {
      queryParams: { checkIn, checkOut, adults, children }
    });
  }

  scrollTo(id: string): void {
    document.getElementById(id)?.scrollIntoView({ behavior: 'smooth' });
  }

  roomImage(type: RoomType): string {
    return ROOM_IMAGES[type.name] ?? '';
  }

  // Görsel bulunamazsa gizle → arkadaki renkli yer tutucu görünür
  hideBrokenImage(event: Event): void {
    (event.target as HTMLImageElement).style.visibility = 'hidden';
  }

  // ---------- Hata mesajları ----------
  private showError(name: string): boolean {
    const control = this.searchForm.get(name);
    return !!control && control.invalid && (control.touched || this.submitted());
  }

  checkInError(): string | null {
    if (!this.showError('checkIn')) return null;
    const errors = this.searchForm.get('checkIn')?.errors;
    if (errors?.['required']) return 'Giriş tarihi seçin.';
    if (errors?.['pastDate']) return 'Geçmiş bir tarih seçilemez.';
    return null;
  }

  checkOutError(): string | null {
    const control = this.searchForm.get('checkOut');
    const visible = control?.touched || this.submitted();
    if (!visible) return null;
    if (control?.errors?.['required']) return 'Çıkış tarihi seçin.';
    if (this.searchForm.errors?.['dateOrder']) return 'Çıkış tarihi giriş tarihinden sonra olmalıdır.';
    return null;
  }

  guestError(): string | null {
    const visible =
      this.searchForm.get('adults')?.touched ||
      this.searchForm.get('children')?.touched ||
      this.submitted();
    if (!visible) return null;
    if (this.searchForm.get('adults')?.invalid) return 'En az 1 yetişkin olmalıdır.';
    if (this.searchForm.get('children')?.invalid) return 'Çocuk sayısı 0 veya daha fazla olmalıdır.';
    if (this.searchForm.errors?.['tooManyGuests']) return `Bir odada en fazla ${MAX_GUESTS} kişi konaklayabilir.`;
    return null;
  }
}