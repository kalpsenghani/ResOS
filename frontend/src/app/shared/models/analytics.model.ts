export interface RevenueAnalytics {
  totalRevenue: number;
  orderCount: number;
  avgOrderValue: number;
  changePercent: number;
  labels: string[];
  values: number[];
}

export interface CategoryStockSummary {
  category: string;
  itemCount: number;
  stockValue: number;
}

export interface InventoryAnalytics {
  totalItems: number;
  lowStockItems: number;
  inventoryValue: number;
  wasteTransactions: number;
  wasteCost: number;
  usageTransactions: number;
  topCategories: CategoryStockSummary[];
}

export interface PositionSummary {
  position: string;
  employeeCount: number;
  scheduledShifts: number;
}

export interface EmployeeAnalytics {
  activeEmployees: number;
  scheduledShifts: number;
  totalHours: number;
  estimatedLaborCost: number;
  byPosition: PositionSummary[];
}

export interface PeakHourSummary {
  hour: number;
  orderCount: number;
}

export interface StatusSummary {
  status: string;
  count: number;
}

export interface OrderAnalytics {
  totalOrders: number;
  completedOrders: number;
  avgTicket: number;
  peakHours: PeakHourSummary[];
  byStatus: StatusSummary[];
}
