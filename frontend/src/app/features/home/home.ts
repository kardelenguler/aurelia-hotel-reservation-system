import { Component, OnInit, inject, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { RoomService } from '../../core/services/room.service';
import { RoomType } from '../../core/models/room.model';
import { hideBrokenImage, roomImageFor } from '../../core/utils/room-images';
import { SearchBox } from '../../shared/components/search-box/search-box';

@Component({
  selector: 'app-home',
  imports: [RouterLink, CurrencyPipe, SearchBox],
  templateUrl: './home.html',
  styleUrl: './home.css'
})
export class Home implements OnInit {
  private readonly roomService = inject(RoomService);

  // ---------- Oda tipleri ----------
  protected readonly roomTypes = signal<RoomType[]>([]);
  protected readonly loading = signal(true);
  protected readonly loadError = signal(false);

  protected readonly highlights = [
    { icon: 'dining', title: 'Her Şey Dahil', text: 'Yeme, içme ve eğlence tek pakette' },
    { icon: 'lotus', title: 'Lüks Konaklama', text: 'Konforlu ve şık odalar' },
    { icon: 'waves', title: 'Deniz & Havuz', text: 'Özel plaj ve geniş havuzlar' },
    { icon: 'spa', title: 'Spa & Wellness', text: 'Ruhunuzu ve bedeninizi yenileyin' }
  ];

  protected readonly galleryImages = [
    { src: '/images/aurelia-gallery-1.jpg', alt: 'Palmiyelerle çevrili sonsuzluk havuzu' },
    { src: '/images/aurelia-gallery-2.jpg', alt: 'Deniz manzaralı açık hava restoranı' },
    { src: '/images/aurelia-gallery-3.jpg', alt: 'Özel plaj, şezlonglar ve iskele' },
    { src: '/images/aurelia-gallery-4.jpg', alt: 'Spa ve kapalı havuz' }
  ];

  protected readonly roomImage = (type: RoomType) => roomImageFor(type.name);
  protected readonly hideBrokenImage = hideBrokenImage;

  ngOnInit(): void {
    this.loadRoomTypes();
  }

  loadRoomTypes(): void {
    this.loading.set(true);
    this.loadError.set(false);

    this.roomService.getRoomTypes().subscribe({
      next: types => {
        this.roomTypes.set(types);
        this.loading.set(false);
      },
      error: err => {
        console.error('Oda tipleri yüklenemedi:', err);
        this.loadError.set(true);
        this.loading.set(false);
      }
    });
  }

  scrollTo(id: string): void {
    document.getElementById(id)?.scrollIntoView({ behavior: 'smooth' });
  }
}