import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { API_URL } from '../api.config';
import { AuthResponse, LoginRequest, RegisterRequest, SessionUser } from '../models/auth.model';

const STORAGE_KEY = 'aurelia_session';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);

  // Oturum bilgisi (signal: değişince ekran otomatik güncellenir)
  private readonly session = signal<SessionUser | null>(this.loadSession());

  readonly currentUser = this.session.asReadonly();
  readonly isLoggedIn = computed(() => this.session() !== null);
  readonly isAdmin = computed(() => this.session()?.role === 'admin');
  readonly isStaff = computed(() => {
    const role = this.session()?.role;
    return role === 'admin' || role === 'receptionist';
  });
  readonly firstName = computed(() => this.session()?.fullName.split(' ')[0] ?? '');

  // Geçerli token (süresi dolduysa oturumu kapatır)
  get token(): string | null {
    const current = this.session();
    if (!current) {
      return null;
    }
    if (Date.now() >= current.expiresAt) {
      this.logout();
      return null;
    }
    return current.token;
  }

  // Giriş: başarılıysa oturumu otomatik kaydeder
  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${API_URL}/auth/login`, credentials)
      .pipe(tap(response => this.saveSession(response)));
  }

  // Kayıt: her zaman müşteri olarak oluşturulur (rolü backend belirler)
  register(data: RegisterRequest): Observable<unknown> {
    return this.http.post(`${API_URL}/auth/register`, data);
  }

  logout(): void {
    try {
      localStorage.removeItem(STORAGE_KEY);
    } catch (error) {
      console.error('Oturum silinemedi.', error);
    }
    this.session.set(null);
  }

  private saveSession(response: AuthResponse): void {
    const sessionUser: SessionUser = {
      userId: response.userId,
      fullName: response.fullName,
      email: response.email,
      role: response.role,
      token: response.token,
      expiresAt: Date.now() + response.expiresIn * 1000
    };

    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(sessionUser));
    } catch (error) {
      console.error('Oturum tarayıcıya kaydedilemedi.', error);
    }
    this.session.set(sessionUser);
  }

  // Sayfa yenilenince kayıtlı oturumu oku — bozuk/eski veri varsa temizle
  private loadSession(): SessionUser | null {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      if (!raw) {
        return null;
      }

      const parsed = JSON.parse(raw) as SessionUser;
      const isValid =
        parsed !== null &&
        typeof parsed.token === 'string' &&
        typeof parsed.expiresAt === 'number' &&
        Date.now() < parsed.expiresAt;

      if (!isValid) {
        localStorage.removeItem(STORAGE_KEY);
        return null;
      }
      return parsed;
    } catch (error) {
      // JSON bozuksa uygulama çökmesin, oturumu temizle
      console.warn('Kayıtlı oturum okunamadı, temizleniyor.', error);
      try {
        localStorage.removeItem(STORAGE_KEY);
      } catch {
        // tarayıcı depolamaya izin vermiyorsa yapılacak bir şey yok
      }
      return null;
    }
  }
}