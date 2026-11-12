import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from '../api.config';
import {
  CancellationPreview,
  CreateReservationRequest,
  Payment,
  PaymentMethod,
  Reservation
} from '../models/reservation.model';

@Injectable({ providedIn: 'root' })
export class ReservationService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${API_URL}/reservations`;

  create(request: CreateReservationRequest): Observable<Reservation> {
    return this.http.post<Reservation>(this.baseUrl, request);
  }

  getById(id: number): Observable<Reservation> {
    return this.http.get<Reservation>(`${this.baseUrl}/${id}`);
  }

  getMyReservations(): Observable<Reservation[]> {
    return this.http.get<Reservation[]>(`${this.baseUrl}/my`);
  }

  // Sadece ödeme yöntemi gönderilir — kart bilgileri ASLA backend'e gitmez
  pay(reservationId: number, method: PaymentMethod): Observable<Payment> {
    return this.http.post<Payment>(`${this.baseUrl}/${reservationId}/payment`, { method });
  }

  getPayment(reservationId: number): Observable<Payment> {
    return this.http.get<Payment>(`${this.baseUrl}/${reservationId}/payment`);
  }

  previewCancellation(reservationId: number): Observable<CancellationPreview> {
    return this.http.get<CancellationPreview>(`${this.baseUrl}/${reservationId}/cancellation-preview`);
  }

  cancel(reservationId: number): Observable<Reservation> {
    return this.http.patch<Reservation>(`${this.baseUrl}/${reservationId}/cancel`, {});
  }
}