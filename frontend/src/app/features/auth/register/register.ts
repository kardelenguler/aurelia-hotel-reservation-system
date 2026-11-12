import { Component, inject, input, signal } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
  Validators
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { extractErrorMessage, isSafeReturnUrl } from '../../../core/utils/error.utils';

// Sadece boşluktan oluşan giriş kabul edilmez
const notBlank: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const value = control.value;
  return typeof value === 'string' && value.length > 0 && value.trim().length === 0
    ? { blank: true }
    : null;
};

// Şifre en az bir harf ve bir rakam içermeli
const passwordStrength: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const value = control.value as string;
  if (!value) return null;
  const hasLetter = /[A-Za-zÇĞİÖŞÜçğıöşü]/.test(value);
  const hasDigit = /\d/.test(value);
  return hasLetter && hasDigit ? null : { weak: true };
};

// Şifre ve şifre tekrarı aynı olmalı
const passwordsMatch: ValidatorFn = (group: AbstractControl): ValidationErrors | null => {
  const password = group.get('password')?.value;
  const confirm = group.get('confirmPassword')?.value;
  return password && confirm && password !== confirm ? { mismatch: true } : null;
};

const REQUIRED_MESSAGES: Record<string, string> = {
  firstName: 'Ad zorunludur.',
  lastName: 'Soyad zorunludur.',
  email: 'E-posta zorunludur.',
  password: 'Şifre zorunludur.',
  confirmPassword: 'Şifre tekrarı zorunludur.',
  terms: 'Devam etmek için kullanım koşullarını onaylamalısınız.'
};

@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './register.html',
  styleUrl: '../auth.css'
})
export class Register {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly returnUrl = input<string>();

  protected readonly loading = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly submitted = signal(false);
  protected readonly showPassword = signal(false);

  protected readonly form = this.fb.nonNullable.group(
    {
      firstName: ['', [Validators.required, notBlank, Validators.maxLength(50)]],
      lastName: ['', [Validators.required, notBlank, Validators.maxLength(50)]],
      email: ['', [Validators.required, Validators.email, Validators.maxLength(255)]],
      password: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(72), passwordStrength]],
      confirmPassword: ['', [Validators.required]],
      terms: [false, [Validators.requiredTrue]]
    },
    { validators: passwordsMatch }
  );

  submit(): void {
    this.submitted.set(true);
    this.errorMessage.set(null);

    if (this.form.invalid || this.loading()) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    const { firstName, lastName, email, password } = this.form.getRawValue();
    const cleanEmail = email.trim().toLowerCase();

    this.auth.register({
      fullName: `${firstName.trim()} ${lastName.trim()}`,
      email: cleanEmail,
      password
    }).subscribe({
      next: () => {
        this.loading.set(false);
        const returnUrl = this.returnUrl();
        this.router.navigate(['/login'], {
          queryParams: {
            registered: 1,
            email: cleanEmail,
            returnUrl: isSafeReturnUrl(returnUrl) ? returnUrl : null
          }
        });
      },
      error: err => {
        this.loading.set(false);
        this.errorMessage.set(extractErrorMessage(err, 'Kayıt oluşturulamadı. Lütfen tekrar deneyin.'));
      }
    });
  }

  togglePassword(): void {
    this.showPassword.update(show => !show);
  }

  // Alanın altında gösterilecek hata mesajı
  fieldError(name: string): string | null {
    const control = this.form.get(name);
    if (!control || !(control.touched || this.submitted())) return null;

    // Şifreler eşleşmiyor (form geneli hata, tekrar alanının altında gösterilir)
    if (name === 'confirmPassword' && !control.errors && this.form.errors?.['mismatch']) {
      return 'Şifreler eşleşmiyor.';
    }

    const errors = control.errors;
    if (!errors) return null;
    if (errors['required'] || errors['blank']) return REQUIRED_MESSAGES[name];
    if (errors['email']) return 'Geçerli bir e-posta adresi girin.';
    if (errors['minlength']) return 'Şifre en az 8 karakter olmalıdır.';
    if (errors['maxlength']) return 'Bu alan çok uzun.';
    if (errors['weak']) return 'Şifre en az bir harf ve bir rakam içermelidir.';
    return null;
  }
}