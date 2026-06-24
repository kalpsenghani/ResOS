import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { EmployeeService } from '../services/employee.service';
import {
  CreateEmployeeRequest,
  Employee,
  EmployeeStatus,
  UpdateEmployeeRequest,
} from '../../../shared/models/employee.model';
import { ButtonComponent } from '../../../shared/components/button/button.component';

export interface EmployeeFormData {
  restaurantId: string;
  employee?: Employee;
}

@Component({
  selector: 'app-employee-form',
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDialogModule,
    ButtonComponent,
  ],
  template: `
    <h2 mat-dialog-title>{{ data.employee ? 'Edit employee' : 'Add employee' }}</h2>
    <form [formGroup]="form" mat-dialog-content class="form">
      <div class="row">
        <mat-form-field appearance="outline">
          <mat-label>First name</mat-label>
          <input matInput formControlName="firstName" />
        </mat-form-field>
        <mat-form-field appearance="outline">
          <mat-label>Last name</mat-label>
          <input matInput formControlName="lastName" />
        </mat-form-field>
      </div>

      <div class="row">
        <mat-form-field appearance="outline">
          <mat-label>Email</mat-label>
          <input matInput type="email" formControlName="email" />
        </mat-form-field>
        <mat-form-field appearance="outline">
          <mat-label>Phone</mat-label>
          <input matInput formControlName="phone" />
        </mat-form-field>
      </div>

      <div class="row">
        <mat-form-field appearance="outline">
          <mat-label>Position</mat-label>
          <input matInput formControlName="position" />
        </mat-form-field>
        <mat-form-field appearance="outline">
          <mat-label>Hourly rate</mat-label>
          <input matInput type="number" step="0.01" formControlName="hourlyRate" />
        </mat-form-field>
      </div>

      <div class="row">
        <mat-form-field appearance="outline">
          <mat-label>Hire date</mat-label>
          <input matInput type="date" formControlName="hireDate" />
        </mat-form-field>
        @if (data.employee) {
          <mat-form-field appearance="outline">
            <mat-label>Status</mat-label>
            <mat-select formControlName="status">
              @for (status of statuses; track status) {
                <mat-option [value]="status">{{ status }}</mat-option>
              }
            </mat-select>
          </mat-form-field>
        }
      </div>

      @if (error()) {
        <p class="error">{{ error() }}</p>
      }
    </form>

    <div mat-dialog-actions align="end">
      <app-button variant="ghost" (clicked)="dialogRef.close()">Cancel</app-button>
      <app-button [disabled]="saving() || form.invalid" (clicked)="submit()">
        {{ saving() ? 'Saving...' : 'Save' }}
      </app-button>
    </div>
  `,
  styles: `
    .form {
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
      min-width: min(520px, 90vw);
    }

    .row {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(10rem, 1fr));
      gap: 0.75rem;
    }

    .error {
      color: var(--color-danger);
      margin: 0;
    }
  `,
})
export class EmployeeFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly employeeService = inject(EmployeeService);
  readonly dialogRef = inject(MatDialogRef<EmployeeFormComponent>);
  readonly data = inject<EmployeeFormData>(MAT_DIALOG_DATA);

  readonly saving = signal(false);
  readonly error = signal<string | null>(null);
  readonly statuses: EmployeeStatus[] = ['ACTIVE', 'ON_LEAVE', 'TERMINATED'];

  readonly form = this.fb.nonNullable.group({
    firstName: ['', Validators.required],
    lastName: ['', Validators.required],
    email: [''],
    phone: [''],
    position: ['', Validators.required],
    hourlyRate: [0, Validators.min(0)],
    hireDate: ['', Validators.required],
    status: ['ACTIVE' as EmployeeStatus],
  });

  ngOnInit(): void {
    const employee = this.data.employee;
    if (employee) {
      this.form.patchValue({
        firstName: employee.firstName,
        lastName: employee.lastName,
        email: employee.email ?? '',
        phone: employee.phone ?? '',
        position: employee.position,
        hourlyRate: employee.hourlyRate ?? 0,
        hireDate: employee.hireDate,
        status: employee.status,
      });
    }
  }

  submit(): void {
    if (this.form.invalid) return;
    this.saving.set(true);
    this.error.set(null);
    const value = this.form.getRawValue();

    if (this.data.employee) {
      const request: UpdateEmployeeRequest = {
        firstName: value.firstName,
        lastName: value.lastName,
        email: value.email || undefined,
        phone: value.phone || undefined,
        position: value.position,
        hourlyRate: value.hourlyRate || undefined,
        hireDate: value.hireDate,
        status: value.status,
      };
      this.employeeService.update(this.data.employee.id, request, this.data.employee.version).subscribe({
        next: () => this.dialogRef.close(true),
        error: () => {
          this.error.set('Failed to update employee.');
          this.saving.set(false);
        },
      });
    } else {
      const request: CreateEmployeeRequest = {
        restaurantId: this.data.restaurantId,
        firstName: value.firstName,
        lastName: value.lastName,
        email: value.email || undefined,
        phone: value.phone || undefined,
        position: value.position,
        hourlyRate: value.hourlyRate || undefined,
        hireDate: value.hireDate,
      };
      this.employeeService.create(request).subscribe({
        next: () => this.dialogRef.close(true),
        error: () => {
          this.error.set('Failed to create employee.');
          this.saving.set(false);
        },
      });
    }
  }
}
