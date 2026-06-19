export type KpiTrend = 'UP' | 'DOWN' | 'FLAT';

export interface KpiMetric {
  value: number;
  change: number;
  trend: KpiTrend;
}

export interface DashboardKpis {
  revenue: KpiMetric;
  orders: KpiMetric;
  reservations: KpiMetric;
  lowStockItems: KpiMetric;
  activeEmployees: KpiMetric;
  avgOrderValue: KpiMetric;
}

export interface RecentOrder {
  id: string;
  orderNumber: string;
  customerName: string;
  status: string;
  totalAmount: number;
  createdAt: string;
}

export interface RevenueChart {
  labels: string[];
  values: number[];
}
