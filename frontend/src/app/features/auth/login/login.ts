import { Component, OnInit, inject, input, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { extractErrorMessage, isSafeReturnUrl } from '../../../core/utils/error.utils';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: '../auth.css'
})
export class Login implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  // URL parametreleri
  readonly returnUrl = input<string>();
  readonly sessionExpired = input<string>();
  readonly registered = input<string>();
  readonly email = input<string>();

  protected readonly loading = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly submitted = signal(false);
  protected readonly showPassword = signal(false);

  protected readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]]
  });

  ngOnInit(): void {
    // Kayıttan geldiyse e-postayı hazır doldur
    const email = this.email();
    if (email) {
      this.form.patchValue({ email });
    }
  }

  submit(): void {
    this.submitted.set(true);
    this.errorMessage.set(null);

    if (this.form.invalid || this.loading()) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    const { email, password } = this.form.getRawValue();

    this.auth.login({ email: email.trim(), password }).subscribe({
      next: response => {
        this.loading.set(false);
        const returnUrl = this.returnUrl();
        const isStaff = response.role === 'admin' || response.role === 'receptionist';

        if (isSafeReturnUrl(returnUrl)) {
          this.router.navigateByUrl(returnUrl);
        } else {
          this.router.navigate([isStaff ? '/admin/dashboard' : '/']);
        }
      },
      error: err => {
        this.loading.set(false);
        this.errorMessage.set(extractErrorMessage(err, 'Giriş yapılamadı. Lütfen tekrar deneyin.'));
      }
    });
  }

  togglePassword(): void {
    this.showPassword.update(show => !show);
  }

  // ---------- Hata mesajları ----------
  emailError(): string | null {
    const control = this.form.controls.email;
    if (!control.invalid || !(control.touched || this.submitted())) return null;
    if (control.errors?.['required']) return 'E-posta zorunludur.';
    if (control.errors?.['email']) return 'Geçerli bir e-posta adresi girin.';
    return null;
  }

  passwordError(): string | null {
    const control = this.form.controls.password;
    if (!control.invalid || !(control.touched || this.submitted())) return null;
    return 'Şifre zorunludur.';
  }
}