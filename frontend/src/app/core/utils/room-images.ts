// Oda tipi adına göre görsel (frontend/public/images içinde)
const ROOM_IMAGES: Record<string, string> = {
  'Standard Room': '/images/aurelia-room-standard.jpg',
  'Deluxe Sea View': '/images/aurelia-room-deluxe.jpg',
  'Family Suite': '/images/aurelia-room-family.jpg',
  'King Suite': '/images/aurelia-room-king.jpg'
};

export function roomImageFor(roomTypeName: string): string {
  return ROOM_IMAGES[roomTypeName] ?? '';
}

// Görsel bulunamazsa gizle → arkadaki renkli yer tutucu görünür
export function hideBrokenImage(event: Event): void {
  (event.target as HTMLImageElement).style.visibility = 'hidden';
}