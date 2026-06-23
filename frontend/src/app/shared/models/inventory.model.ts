export interface InventoryItem {
  id: string;
  name: string;
  sku?: string;
  category?: string;
  unit: string;
  currentStock: number;
  minimumStock: number;
  maximumStock?: number;
  unitCost?: number;
  supplier?: string;
  expiryDate?: string;
  isLowStock: boolean;
  restaurantId: string;
  createdAt: string;
  updatedAt: string;
  version: number;
}

export interface CreateInventoryItemRequest {
  restaurantId: string;
  name: string;
  sku?: string;
  category?: string;
  unit: string;
  currentStock?: number;
  minimumStock?: number;
  maximumStock?: number;
  unitCost?: number;
  supplier?: string;
}

export interface UpdateInventoryItemRequest {
  name?: string;
  sku?: string;
  category?: string;
  unit?: string;
  currentStock?: number;
  minimumStock?: number;
  maximumStock?: number;
  unitCost?: number;
  supplier?: string;
  expiryDate?: string;
}

export type TransactionType = 'PURCHASE' | 'USAGE' | 'WASTE' | 'ADJUSTMENT' | 'TRANSFER';

export interface CreateTransactionRequest {
  type: TransactionType;
  quantity: number;
  unitCost?: number;
  reference?: string;
  notes?: string;
}

export interface InventoryTransaction {
  id: string;
  inventoryItemId: string;
  type: TransactionType;
  quantity: number;
  unitCost?: number;
  reference?: string;
  notes?: string;
  performedBy?: string;
  createdAt: string;
}

export type AlertType = 'LOW_STOCK' | 'OUT_OF_STOCK' | 'EXPIRING';

export interface StockAlert {
  id: string;
  inventoryItemId: string;
  itemName: string;
  alertType: AlertType;
  message: string;
  acknowledged: boolean;
  acknowledgedBy?: string;
  acknowledgedAt?: string;
  createdAt: string;
}

export interface PaginatedMeta {
  page?: number;
  size?: number;
  totalElements?: number;
  totalPages?: number;
}
