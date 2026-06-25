import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { InventoryService } from '../services/inventory.service';
import { CreateInventoryItemRequest, InventoryItem, TransactionType } from '../../../shared/models/inventory.model';
import { ButtonComponent } from '../../../shared/components/button/button.component';
import { SelectComponent, SelectOption } from '../../../shared/components/select/select.component';

export interface InventoryFormData {
  restaurantId: string;
  item?: InventoryItem;
}

@Component({
  selector: 'app-inventory-form',
  imports: [ReactiveFormsModule, MatDialogModule, ButtonComponent, SelectComponent],
  template: `
    <h2 mat-dialog-title>{{ data.item ? 'Edit item' : 'Add inventory item' }}</h2>
    <form [formGroup]="form" (ngSubmit)="submit()" mat-dialog-content class="form">
      <label class="field">
        <span class="field-label">Name</span>
        <input class="input" formControlName="name" />
      </label>

      <div class="row">
        <label class="field">
          <span class="field-label">SKU</span>
          <input class="input" formControlName="sku" />
        </label>
        <label class="field">
          <span class="field-label">Category</span>
          <input class="input" formControlName="category" />
        </label>
      </div>

      <div class="row">
        <label class="field">
          <span class="field-label">Unit</span>
          <input class="input" formControlName="unit" />
        </label>
        <label class="field">
          <span class="field-label">Supplier</span>
          <input class="input" formControlName="supplier" />
        </label>
      </div>

      <div class="row">
        <label class="field">
          <span class="field-label">Current stock</span>
          <input class="input" type="number" formControlName="currentStock" />
        </label>
        <label class="field">
          <span class="field-label">Minimum stock</span>
          <input class="input" type="number" formControlName="minimumStock" />
        </label>
        <label class="field">
          <span class="field-label">Unit cost</span>
          <input class="input" type="number" step="0.01" formControlName="unitCost" />
        </label>
      </div>

      @if (data.item) {
        <h3>Record transaction</h3>
        <div class="row">
          <label class="field">
            <span class="field-label">Type</span>
            <app-select formControlName="transactionType" [options]="transactionTypeOptions" placeholder="Select type" />
          </label>
          <label class="field">
            <span class="field-label">Quantity</span>
            <input class="input" type="number" formControlName="transactionQty" />
          </label>
        </div>
      }

      @if (error()) {
        <p class="error">{{ error() }}</p>
      }
    </form>

    <div mat-dialog-actions align="end">
      <app-button variant="ghost" (clicked)="dialogRef.close()">Cancel</app-button>
      <app-button type="submit" [disabled]="saving() || form.invalid" (clicked)="submit()">
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
      grid-template-columns: repeat(auto-fit, minmax(8rem, 1fr));
      gap: 0.75rem;
    }

    h3 {
      margin: 1rem 0 0.25rem;
      font-size: 0.9375rem;
    }

    .error {
      color: var(--color-danger);
      margin: 0;
    }
  `,
})
export class InventoryFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly inventoryService = inject(InventoryService);
  readonly dialogRef = inject(MatDialogRef<InventoryFormComponent>);
  readonly data = inject<InventoryFormData>(MAT_DIALOG_DATA);

  readonly saving = signal(false);
  readonly error = signal<string | null>(null);
  readonly transactionTypes: TransactionType[] = ['PURCHASE', 'USAGE', 'WASTE', 'ADJUSTMENT', 'TRANSFER'];
  readonly transactionTypeOptions: SelectOption[] = this.transactionTypes.map((type) => ({
    value: type,
    label: type.charAt(0) + type.slice(1).toLowerCase(),
  }));

  readonly form = this.fb.nonNullable.group({
    name: ['', Validators.required],
    sku: [''],
    category: [''],
    unit: ['kg', Validators.required],
    supplier: [''],
    currentStock: [0, [Validators.required, Validators.min(0)]],
    minimumStock: [0, [Validators.required, Validators.min(0)]],
    unitCost: [0, Validators.min(0)],
    transactionType: ['PURCHASE' as TransactionType],
    transactionQty: [0, Validators.min(0)],
  });

  ngOnInit(): void {
    const item = this.data.item;
    if (item) {
      this.form.patchValue({
        name: item.name,
        sku: item.sku ?? '',
        category: item.category ?? '',
        unit: item.unit,
        supplier: item.supplier ?? '',
        currentStock: item.currentStock,
        minimumStock: item.minimumStock,
        unitCost: item.unitCost ?? 0,
      });
    }
  }

  submit(): void {
    if (this.form.invalid) return;
    this.saving.set(true);
    this.error.set(null);

    const value = this.form.getRawValue();

    if (this.data.item) {
      this.inventoryService
        .update(this.data.item.id, {
          name: value.name,
          sku: value.sku || undefined,
          category: value.category || undefined,
          unit: value.unit,
          supplier: value.supplier || undefined,
          currentStock: value.currentStock,
          minimumStock: value.minimumStock,
          unitCost: value.unitCost || undefined,
        }, this.data.item.version)
        .subscribe({
          next: () => this.afterSave(value.transactionQty),
          error: () => {
            this.error.set('Failed to update item.');
            this.saving.set(false);
          },
        });
    } else {
      const request: CreateInventoryItemRequest = {
        restaurantId: this.data.restaurantId,
        name: value.name,
        sku: value.sku || undefined,
        category: value.category || undefined,
        unit: value.unit,
        supplier: value.supplier || undefined,
        currentStock: value.currentStock,
        minimumStock: value.minimumStock,
        unitCost: value.unitCost || undefined,
      };
      this.inventoryService.create(request).subscribe({
        next: () => this.dialogRef.close(true),
        error: () => {
          this.error.set('Failed to create item.');
          this.saving.set(false);
        },
      });
    }
  }

  private afterSave(transactionQty: number): void {
    if (!this.data.item || transactionQty <= 0) {
      this.dialogRef.close(true);
      return;
    }

    const type = this.form.getRawValue().transactionType;
    this.inventoryService.recordTransaction(this.data.item.id, {
      type,
      quantity: transactionQty,
    }).subscribe({
      next: () => this.dialogRef.close(true),
      error: () => {
        this.error.set('Item saved but transaction failed.');
        this.saving.set(false);
      },
    });
  }
}
