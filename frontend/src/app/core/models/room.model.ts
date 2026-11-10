// Backend /api/room-types cevabı
export interface RoomType {
  id: number;
  name: string;
  description: string | null;
  capacity: number;
  pricePerNight: number;
}

// Backend /api/rooms/... cevabı
export interface Room {
  id: number;
  roomNumber: string;
  floor: number;
  isActive: boolean;
  roomType: RoomType;
}