import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from '../api.config';
import { Room, RoomType } from '../models/room.model';

@Injectable({ providedIn: 'root' })
export class RoomService {
  private readonly http = inject(HttpClient);

  // Tüm oda tipleri (herkese açık)
  getRoomTypes(): Observable<RoomType[]> {
    return this.http.get<RoomType[]>(`${API_URL}/room-types`);
  }

  // Müsait odalar (herkese açık)
  searchAvailable(checkIn: string, checkOut: string, guests: number): Observable<Room[]> {
    const params = new HttpParams()
      .set('checkIn', checkIn)
      .set('checkOut', checkOut)
      .set('guests', guests);
    return this.http.get<Room[]>(`${API_URL}/rooms/available`, { params });
  }
}