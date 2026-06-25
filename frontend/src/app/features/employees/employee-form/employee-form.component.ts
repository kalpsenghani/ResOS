import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { EmployeeService } from '../services/employee.service';
import {
  CreateEmployeeRequest,
  Employee,
  EmployeeStatus,
  UpdateEmployeeRequest,
} from '../../../shared/models/employee.model';
import { ButtonComponent } from '../../../shared/components/button/button.component';
import { SelectComponent, SelectOption } from '../../../shared/components/select/select.component';

export interface EmployeeFormData {
  restaurantId: string;
  employee?: Employee;
}

@Component({
  selector: 'app-employee-form',
  imports: [ReactiveFormsModule, MatDialogModule, ButtonComponent, SelectComponent],
  template: `
    <h2 mat-dialog-title>{{ data.employee ? 'Edit employee' : 'Add employee' }}</h2>
    <form [formGroup]="form" mat-dialog-content class="form">
      <div class="row">
        <label class="field">
          <span class="field-label">First name</span>
          <input class="input" formControlName="firstName" />
        </label>
        <label class="field">
          <span class="field-label">Last name</span>
          <input class="input" formControlName="lastName" />
        </label>
      </div>

      <div class="row">
        <label class="field">
          <span class="field-label">Email</span>
          <input class="input" type="email" formControlName="email" />
        </label>
        <label class="field">
          <span class="field-label">Phone</span>
          <input class="input" formControlName="phone" />
        </label>
      </div>

      <div class="row">
        <label class="field">
          <span class="field-label">Position</span>
          <input class="input" formControlName="position" />
        </label>
        <label class="field">
          <span class="field-label">Hourly rate</span>
          <input class="input" type="number" step="0.01" formControlName="hourlyRate" />
        </label>
      </div>

      <div class="row">
        <label class="field">
          <span class="field-label">Hire date</span>
          <input class="input" type="date" formControlName="hireDate" />
        </label>
        @if (data.employee) {
          <label class="field">
            <span class="field-label">Status</span>
            <app-select formControlName="status" [options]="statusOptions" placeholder="Select status" />
          </label>
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
  readonly statusOptions: SelectOption[] = this.statuses.map((status) => ({
    value: status,
    label: status.replace('_', ' '),
  }));

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
