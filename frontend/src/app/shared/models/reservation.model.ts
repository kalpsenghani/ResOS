export type ReservationStatus =
  | 'PENDING'
  | 'CONFIRMED'
  | 'SEATED'
  | 'COMPLETED'
  | 'CANCELLED'
  | 'NO_SHOW';

export interface RestaurantTable {
  id: string;
  restaurantId: string;
  tableNumber: string;
  capacity: number;
  location?: string;
  active: boolean;
  createdAt: string;
}

export interface Reservation {
  id: string;
  restaurantId: string;
  tableId?: string;
  tableNumber?: string;
  guestName: string;
  guestPhone?: string;
  guestEmail?: string;
  partySize: number;
  reservationDate: string;
  startTime: string;
  endTime?: string;
  status: ReservationStatus;
  specialRequests?: string;
  createdAt: string;
  updatedAt: string;
  version: number;
}

export interface CreateTableRequest {
  restaurantId: string;
  tableNumber: string;
  capacity: number;
  location?: string;
}

export interface UpdateTableRequest {
  tableNumber?: string;
  capacity?: number;
  location?: string;
  active?: boolean;
}

export interface CreateReservationRequest {
  restaurantId: string;
  tableId?: string;
  guestName: string;
  guestPhone?: string;
  guestEmail?: string;
  partySize: number;
  reservationDate: string;
  startTime: string;
  endTime?: string;
  specialRequests?: string;
}

export interface UpdateReservationRequest {
  tableId?: string;
  guestName?: string;
  guestPhone?: string;
  guestEmail?: string;
  partySize?: number;
  reservationDate?: string;
  startTime?: string;
  endTime?: string;
  status?: ReservationStatus;
  specialRequests?: string;
}

export interface AvailabilityResponse {
  available: boolean;
  suggestedTables: { id: string; tableNumber: string; capacity: number }[];
}
