// customer = müşteri, receptionist = resepsiyon, admin = yönetici
export type UserRole = 'customer' | 'receptionist' | 'admin';

// Backend /api/auth/login cevabı
export interface AuthResponse {
  token: string;
  tokenType: string;
  expiresIn: number; // saniye
  userId: number;
  fullName: string;
  email: string;
  role: UserRole;
}

// Tarayıcıda sakladığımız oturum bilgisi
export interface SessionUser {
  userId: number;
  fullName: string;
  email: string;
  role: UserRole;
  token: string;
  expiresAt: number; // milisaniye cinsinden bitiş zamanı
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  fullName: string;
  email: string;
  password: string;
}