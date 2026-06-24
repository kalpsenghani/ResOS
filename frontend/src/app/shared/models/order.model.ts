export type OrderStatus =
  | 'PENDING'
  | 'CONFIRMED'
  | 'PREPARING'
  | 'READY'
  | 'SERVED'
  | 'COMPLETED'
  | 'CANCELLED';

export type OrderType = 'DINE_IN' | 'TAKEOUT' | 'DELIVERY';

export type OrderItemStatus = 'PENDING' | 'PREPARING' | 'READY' | 'SERVED';

export interface OrderItemModifier {
  name: string;
  priceAdjustment: number;
}

export interface OrderItem {
  id: string;
  menuItemId: string;
  menuItemName?: string;
  quantity: number;
  unitPrice: number;
  modifiers: OrderItemModifier[];
  specialInstructions?: string;
  status: OrderItemStatus;
  createdAt: string;
}

export interface Order {
  id: string;
  restaurantId: string;
  orderNumber: string;
  tableId?: string;
  reservationId?: string;
  customerName?: string;
  orderType: OrderType;
  status: OrderStatus;
  subtotal: number;
  taxAmount: number;
  tipAmount: number;
  totalAmount: number;
  notes?: string;
  items: OrderItem[];
  createdAt: string;
  updatedAt: string;
  completedAt?: string;
  version: number;
}

export interface CreateOrderItemRequest {
  menuItemId: string;
  quantity: number;
  modifiers?: OrderItemModifier[];
  specialInstructions?: string;
}

export interface CreateOrderRequest {
  restaurantId: string;
  tableId?: string;
  reservationId?: string;
  customerName?: string;
  orderType?: OrderType;
  items: CreateOrderItemRequest[];
  notes?: string;
}
