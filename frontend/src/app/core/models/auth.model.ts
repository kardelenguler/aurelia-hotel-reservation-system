// Backend /api/auth/login cevabı
export interface AuthResponse {
  token: string;
  tokenType: string;
  expiresIn: number; // saniye
  userId: number;
  fullName: string;
  email: string;
  role: 'customer' | 'admin';
}

// Tarayıcıda sakladığımız oturum bilgisi
export interface SessionUser {
  userId: number;
  fullName: string;
  email: string;
  role: 'customer' | 'admin';
  token: string;
  expiresAt: number; // milisaniye cinsinden bitiş zamanı
}