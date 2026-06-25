import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { ReservationService } from '../services/reservation.service';
import { CreateTableRequest, RestaurantTable, UpdateTableRequest } from '../../../shared/models/reservation.model';
import { ButtonComponent } from '../../../shared/components/button/button.component';

export interface TableFormData {
  restaurantId: string;
  table?: RestaurantTable;
}

@Component({
  selector: 'app-table-form',
  imports: [ReactiveFormsModule, MatDialogModule, ButtonComponent],
  template: `
    <h2 mat-dialog-title>{{ data.table ? 'Edit table' : 'Add table' }}</h2>
    <form [formGroup]="form" mat-dialog-content class="form">
      <label class="field">
        <span class="field-label">Table number</span>
        <input class="input" formControlName="tableNumber" />
      </label>
      <label class="field">
        <span class="field-label">Capacity</span>
        <input class="input" type="number" min="1" formControlName="capacity" />
      </label>
      <label class="field">
        <span class="field-label">Location</span>
        <input class="input" formControlName="location" placeholder="Main Floor, Patio, Bar..." />
      </label>
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
      gap: 0.75rem;
      min-width: min(360px, 90vw);
    }

    .error {
      color: var(--color-danger);
      margin: 0;
    }
  `,
})
export class TableFormComponent {
  private readonly fb = inject(FormBuilder);
  private readonly reservationService = inject(ReservationService);
  readonly dialogRef = inject(MatDialogRef<TableFormComponent>);
  readonly data = inject<TableFormData>(MAT_DIALOG_DATA);

  readonly saving = signal(false);
  readonly error = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    tableNumber: [this.data.table?.tableNumber ?? '', Validators.required],
    capacity: [this.data.table?.capacity ?? 2, [Validators.required, Validators.min(1)]],
    location: [this.data.table?.location ?? ''],
  });

  submit(): void {
    if (this.form.invalid) return;
    this.saving.set(true);
    this.error.set(null);
    const value = this.form.getRawValue();

    if (this.data.table) {
      const request: UpdateTableRequest = {
        tableNumber: value.tableNumber,
        capacity: value.capacity,
        location: value.location || undefined,
      };
      this.reservationService.updateTable(this.data.table.id, request).subscribe({
        next: () => this.dialogRef.close(true),
        error: () => {
          this.error.set('Failed to update table.');
          this.saving.set(false);
        },
      });
    } else {
      const request: CreateTableRequest = {
        restaurantId: this.data.restaurantId,
        tableNumber: value.tableNumber,
        capacity: value.capacity,
        location: value.location || undefined,
      };
      this.reservationService.createTable(request).subscribe({
        next: () => this.dialogRef.close(true),
        error: () => {
          this.error.set('Failed to create table.');
          this.saving.set(false);
        },
      });
    }
  }
}
