export type EmployeeStatus = 'ACTIVE' | 'ON_LEAVE' | 'TERMINATED';
export type ScheduleStatus = 'SCHEDULED' | 'COMPLETED' | 'NO_SHOW' | 'CANCELLED';

export interface Employee {
  id: string;
  restaurantId: string;
  firstName: string;
  lastName: string;
  email?: string;
  phone?: string;
  position: string;
  hourlyRate?: number;
  hireDate: string;
  status: EmployeeStatus;
  createdAt: string;
  updatedAt: string;
  version: number;
}

export interface CreateEmployeeRequest {
  restaurantId: string;
  firstName: string;
  lastName: string;
  email?: string;
  phone?: string;
  position: string;
  hourlyRate?: number;
  hireDate: string;
}

export interface UpdateEmployeeRequest {
  firstName?: string;
  lastName?: string;
  email?: string;
  phone?: string;
  position?: string;
  hourlyRate?: number;
  hireDate?: string;
  status?: EmployeeStatus;
}

export interface EmployeeSchedule {
  id: string;
  employeeId: string;
  restaurantId: string;
  shiftDate: string;
  startTime: string;
  endTime: string;
  status: ScheduleStatus;
  notes?: string;
  createdAt: string;
}

export interface CreateScheduleRequest {
  restaurantId: string;
  shiftDate: string;
  startTime: string;
  endTime: string;
  notes?: string;
}
