// Otelin en büyük odası (Family Suite) 5 kişilik
export const MAX_GUESTS = 5;

// Bugünün tarihi (yerel saat, YYYY-AA-GG) — toISOString UTC kaydırması yapmasın diye elle
export function todayString(): string {
  const d = new Date();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${d.getFullYear()}-${month}-${day}`;
}

// İki tarih arasındaki gece sayısı
export function nightsBetween(checkIn: string, checkOut: string): number {
  const start = new Date(`${checkIn}T00:00:00`);
  const end = new Date(`${checkOut}T00:00:00`);
  return Math.round((end.getTime() - start.getTime()) / 86_400_000);
}

// URL'den gelen metni güvenli şekilde tam sayıya çevir
export function toInt(value: string | null | undefined, fallback: number): number {
  const parsed = Number(value);
  return Number.isInteger(parsed) ? parsed : fallback;
}