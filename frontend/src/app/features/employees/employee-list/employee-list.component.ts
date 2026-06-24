import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatTabsModule } from '@angular/material/tabs';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { CardComponent } from '../../../shared/components/card/card.component';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { ButtonComponent } from '../../../shared/components/button/button.component';
import { EmployeeService } from '../services/employee.service';
import { RestaurantService } from '../../../core/restaurant/restaurant.service';
import { Employee, EmployeeSchedule } from '../../../shared/models/employee.model';
import { EmployeeFormComponent } from '../employee-form/employee-form.component';

@Component({
  selector: 'app-employee-list',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatTabsModule,
    PageHeaderComponent,
    CardComponent,
    LoadingSpinnerComponent,
    EmptyStateComponent,
    StatusBadgeComponent,
    ButtonComponent,
    CurrencyPipe,
    DatePipe,
  ],
  templateUrl: './employee-list.component.html',
  styleUrl: './employee-list.component.scss',
})
export class EmployeeListComponent implements OnInit {
  private readonly employeeService = inject(EmployeeService);
  private readonly restaurantService = inject(RestaurantService);
  private readonly dialog = inject(MatDialog);
  private readonly fb = inject(FormBuilder);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly employees = signal<Employee[]>([]);
  readonly schedules = signal<EmployeeSchedule[]>([]);
  readonly selectedEmployee = signal<Employee | null>(null);
  readonly restaurantId = signal<string | null>(null);

  readonly scheduleForm = this.fb.nonNullable.group({
    shiftDate: ['', Validators.required],
    startTime: ['09:00', Validators.required],
    endTime: ['17:00', Validators.required],
    notes: [''],
  });

  ngOnInit(): void {
    this.restaurantService.list().subscribe({
      next: (response) => {
        const restaurant = response.data[0];
        if (!restaurant) {
          this.error.set('No restaurant found.');
          this.loading.set(false);
          return;
        }
        this.restaurantId.set(restaurant.id);
        this.loadEmployees();
      },
      error: () => {
        this.error.set('Failed to load restaurant.');
        this.loading.set(false);
      },
    });
  }

  loadEmployees(): void {
    const restaurantId = this.restaurantId();
    if (!restaurantId) return;

    this.loading.set(true);
    this.employeeService.list({ restaurantId, status: 'ACTIVE' }).subscribe({
      next: (response) => {
        this.employees.set(response.data);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Failed to load employees.');
        this.loading.set(false);
      },
    });
  }

  openCreateDialog(): void {
    const restaurantId = this.restaurantId();
    if (!restaurantId) return;

    this.dialog
      .open(EmployeeFormComponent, { data: { restaurantId } })
      .afterClosed()
      .subscribe((saved) => {
        if (saved) this.loadEmployees();
      });
  }

  openEditDialog(employee: Employee): void {
    const restaurantId = this.restaurantId();
    if (!restaurantId) return;

    this.dialog
      .open(EmployeeFormComponent, { data: { restaurantId, employee } })
      .afterClosed()
      .subscribe((saved) => {
        if (saved) this.loadEmployees();
      });
  }

  selectEmployee(employee: Employee): void {
    this.selectedEmployee.set(employee);
    this.employeeService.listSchedules(employee.id).subscribe({
      next: (response) => this.schedules.set(response.data),
    });
  }

  addSchedule(): void {
    const employee = this.selectedEmployee();
    const restaurantId = this.restaurantId();
    if (!employee || !restaurantId || this.scheduleForm.invalid) return;

    const value = this.scheduleForm.getRawValue();
    this.employeeService
      .createSchedule(employee.id, {
        restaurantId,
        shiftDate: value.shiftDate,
        startTime: value.startTime,
        endTime: value.endTime,
        notes: value.notes || undefined,
      })
      .subscribe({
        next: () => {
          this.scheduleForm.reset({ shiftDate: '', startTime: '09:00', endTime: '17:00', notes: '' });
          this.selectEmployee(employee);
        },
      });
  }

  removeSchedule(schedule: EmployeeSchedule): void {
    this.employeeService.deleteSchedule(schedule.id).subscribe({
      next: () => {
        const employee = this.selectedEmployee();
        if (employee) this.selectEmployee(employee);
      },
    });
  }

  statusVariant(status: string): 'success' | 'warning' | 'danger' | 'neutral' {
    if (status === 'ACTIVE') return 'success';
    if (status === 'ON_LEAVE') return 'warning';
    if (status === 'TERMINATED') return 'danger';
    return 'neutral';
  }
}
