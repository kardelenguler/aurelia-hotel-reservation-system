import { Component, OnInit, inject, input, signal } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
  Validators
} from '@angular/forms';
import { Router } from '@angular/router';
import { MAX_GUESTS, todayString } from '../../../core/utils/date.utils';

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

// Ana sayfa ve arama sayfasında ortak kullanılan arama kutusu
@Component({
  selector: 'app-search-box',
  imports: [ReactiveFormsModule],
  templateUrl: './search-box.html',
  styleUrl: './search-box.css',
  host: { '[class.compact]': 'compact()' }
})
export class SearchBox implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);

  // Dışarıdan gelen başlangıç değerleri (arama sayfasında mevcut arama ile doldurulur)
  readonly initialCheckIn = input('');
  readonly initialCheckOut = input('');
  readonly initialAdults = input(2);
  readonly initialChildren = input(0);
  // compact = başlıksız, sayfa içi görünüm
  readonly compact = input(false);

  protected readonly today = todayString();
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

  ngOnInit(): void {
    this.searchForm.patchValue({
      checkIn: this.initialCheckIn(),
      checkOut: this.initialCheckOut(),
      adults: this.initialAdults(),
      children: this.initialChildren()
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

  // ---------- Hata mesajları ----------
  checkInError(): string | null {
    const control = this.searchForm.get('checkIn');
    if (!control?.invalid || !(control.touched || this.submitted())) return null;
    if (control.errors?.['required']) return 'Giriş tarihi seçin.';
    if (control.errors?.['pastDate']) return 'Geçmiş bir tarih seçilemez.';
    return null;
  }

  checkOutError(): string | null {
    const control = this.searchForm.get('checkOut');
    if (!(control?.touched || this.submitted())) return null;
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