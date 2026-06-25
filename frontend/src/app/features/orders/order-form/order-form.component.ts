import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { OrderService } from '../services/order.service';
import { CreateOrderRequest, OrderType } from '../../../shared/models/order.model';
import { MenuItem } from '../../../shared/models/menu.model';
import { ButtonComponent } from '../../../shared/components/button/button.component';
import { SelectComponent, SelectOption } from '../../../shared/components/select/select.component';

export interface OrderFormData {
  restaurantId: string;
  menuItems: MenuItem[];
}

@Component({
  selector: 'app-order-form',
  imports: [ReactiveFormsModule, MatDialogModule, ButtonComponent, SelectComponent],
  template: `
    <h2 mat-dialog-title>New order</h2>
    <form [formGroup]="form" mat-dialog-content class="form">
      <label class="field">
        <span class="field-label">Customer / table</span>
        <input class="input" formControlName="customerName" />
      </label>

      <label class="field">
        <span class="field-label">Order type</span>
        <app-select formControlName="orderType" [options]="orderTypeOptions" placeholder="Select type" />
      </label>

      <label class="field">
        <span class="field-label">Menu item</span>
        <app-select formControlName="menuItemId" [options]="menuItemOptions" placeholder="Select an item" />
      </label>

      <label class="field">
        <span class="field-label">Quantity</span>
        <input class="input" type="number" min="1" formControlName="quantity" />
      </label>

      <label class="field">
        <span class="field-label">Notes</span>
        <textarea class="input" rows="2" formControlName="notes"></textarea>
      </label>

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
  readonly orderTypeOptions: SelectOption[] = this.orderTypes.map((type) => ({
    value: type,
    label: type.replace('_', ' '),
  }));
  readonly menuItemOptions: SelectOption[] = this.data.menuItems.map((item) => ({
    value: item.id,
    label: `${item.name} — $${item.price.toFixed(2)}`,
  }));

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
