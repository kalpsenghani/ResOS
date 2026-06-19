export interface Restaurant {
  id: string;
  name: string;
  address?: string;
  phone?: string;
  email?: string;
  capacity: number;
  openingHours?: Record<string, unknown>;
  active: boolean;
  tenantId: string;
  createdAt: string;
  version: number;
}
