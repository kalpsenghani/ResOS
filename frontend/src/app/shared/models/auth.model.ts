export interface TenantSummary {
  id: string;
  name: string;
  slug: string;
}

export interface UserSummary {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  roles: string[];
  permissions: string[];
}

export interface UserProfile {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  phone?: string;
  status: string;
  roles: string[];
  tenantId?: string;
}

export interface AuthResponse {
  accessToken: string;
  expiresIn: number;
  tokenType: string;
  user: UserSummary;
  tenant: TenantSummary | null;
}

export interface ApiResponse<T> {
  data: T;
  meta?: { timestamp: string };
}

export interface LoginRequest {
  email: string;
  password: string;
  tenantSlug: string;
}

export interface RegisterRequest {
  tenantName: string;
  tenantSlug: string;
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  phone?: string;
}

export interface ApiError {
  error: {
    code: string;
    message: string;
    details?: string[];
  };
}
