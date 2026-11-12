import { ReservationStatus } from '../models/reservation.model';

// Backend durum değerlerinin Türkçe karşılıkları
const STATUS_LABELS: Record<ReservationStatus, string> = {
  pending: 'Ödeme Bekleniyor',
  confirmed: 'Onaylandı',
  checked_in: 'Giriş Yapıldı',
  checked_out: 'Çıkış Yapıldı',
  cancelled: 'İptal Edildi'
};

export function statusLabel(status: ReservationStatus): string {
  return STATUS_LABELS[status] ?? status;
}