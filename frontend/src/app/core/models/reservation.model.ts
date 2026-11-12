import { Room } from './room.model';

export type ReservationStatus = 'pending' | 'confirmed' | 'checked_in' | 'checked_out' | 'cancelled';
export type PaymentMethod = 'card' | 'cash';

export interface ReservationUser {
  id: number;
  fullName: string;
  email: string;
  role: string;
}

// Backend Reservation cevabı
export interface Reservation {
  id: number;
  user: ReservationUser;
  room: Room;
  checkInDate: string;
  checkOutDate: string;
  guestCount: number;
  totalPrice: number;
  status: ReservationStatus;
  createdAt: string;
}

export interface CreateReservationRequest {
  roomId: number;
  checkInDate: string;
  checkOutDate: string;
  guestCount: number;
}

export interface Payment {
  id: number;
  reservationId: number;
  amount: number;
  method: PaymentMethod;
  status: 'paid' | 'refunded';
  refundAmount: number;
  paidAt: string;
}

export interface CancellationPreview {
  cancellable: boolean;
  daysUntilCheckIn: number;
  refundPercent: number;
  paidAmount: number;
  refundAmount: number;
  message: string;
}