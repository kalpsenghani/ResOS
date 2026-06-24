import { CurrencyPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { OrderService } from '../services/order.service';
import { CreateOrderRequest, OrderType } from '../../../shared/models/order.model';
import { MenuItem } from '../../../shared/models/menu.model';
import { ButtonComponent } from '../../../shared/components/button/button.component';

export interface OrderFormData {
  restaurantId: string;
  menuItems: MenuItem[];
}

@Component({
  selector: 'app-order-form',
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDialogModule,
    ButtonComponent,
    CurrencyPipe,
  ],
  template: `
    <h2 mat-dialog-title>New order</h2>
    <form [formGroup]="form" mat-dialog-content class="form">
      <mat-form-field appearance="outline">
        <mat-label>Customer / table</mat-label>
        <input matInput formControlName="customerName" />
      </mat-form-field>

      <mat-form-field appearance="outline">
        <mat-label>Order type</mat-label>
        <mat-select formControlName="orderType">
          @for (type of orderTypes; track type) {
            <mat-option [value]="type">{{ type }}</mat-option>
          }
        </mat-select>
      </mat-form-field>

      <mat-form-field appearance="outline">
        <mat-label>Menu item</mat-label>
        <mat-select formControlName="menuItemId">
          @for (item of data.menuItems; track item.id) {
            <mat-option [value]="item.id">{{ item.name }} — {{ item.price | currency: 'USD' }}</mat-option>
          }
        </mat-select>
      </mat-form-field>

      <mat-form-field appearance="outline">
        <mat-label>Quantity</mat-label>
        <input matInput type="number" min="1" formControlName="quantity" />
      </mat-form-field>

      <mat-form-field appearance="outline">
        <mat-label>Notes</mat-label>
        <textarea matInput rows="2" formControlName="notes"></textarea>
      </mat-form-field>

      @if (error()) {
        <p class="error">{{ error() }}</p>
      }
    </form>
    <div mat-dialog-actions align="end">
      <app-button variant="ghost" (clicked)="dialogRef.close()">Cancel</app-button>
      <app-button [disabled]="saving() || form.invalid" (clicked)="submit()">
        {{ saving() ? 'Saving...' : 'Create order' }}
      </app-button>
    </div>
  `,
  styles: `
    .form {
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
      min-width: min(420px, 90vw);
    }

    .error {
      color: var(--color-danger);
      margin: 0;
    }
  `,
})
export class OrderFormComponent {
  private readonly fb = inject(FormBuilder);
  private readonly orderService = inject(OrderService);
  readonly dialogRef = inject(MatDialogRef<OrderFormComponent>);
  readonly data = inject<OrderFormData>(MAT_DIALOG_DATA);

  readonly saving = signal(false);
  readonly error = signal<string | null>(null);
  readonly orderTypes: OrderType[] = ['DINE_IN', 'TAKEOUT', 'DELIVERY'];

  readonly form = this.fb.nonNullable.group({
    customerName: ['', Validators.required],
    orderType: ['DINE_IN' as OrderType, Validators.required],
    menuItemId: ['', Validators.required],
    quantity: [1, [Validators.required, Validators.min(1)]],
    notes: [''],
  });

  submit(): void {
    if (this.form.invalid) return;
    this.saving.set(true);
    this.error.set(null);
    const value = this.form.getRawValue();

    const request: CreateOrderRequest = {
      restaurantId: this.data.restaurantId,
      customerName: value.customerName,
      orderType: value.orderType,
      notes: value.notes || undefined,
      items: [
        {
          menuItemId: value.menuItemId,
          quantity: value.quantity,
        },
      ],
    };

    this.orderService.create(request).subscribe({
      next: () => this.dialogRef.close(true),
      error: () => {
        this.error.set('Failed to create order.');
        this.saving.set(false);
      },
    });
  }
}
