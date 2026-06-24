import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MenuService } from '../services/menu.service';
import {
  CreateCategoryRequest,
  CreateMenuItemRequest,
  MenuCategory,
  MenuItem,
  UpdateCategoryRequest,
  UpdateMenuItemRequest,
} from '../../../shared/models/menu.model';
import { ButtonComponent } from '../../../shared/components/button/button.component';

export type MenuFormMode = 'category' | 'item';

export interface MenuFormData {
  mode: MenuFormMode;
  restaurantId: string;
  categories: MenuCategory[];
  category?: MenuCategory;
  item?: MenuItem;
}

@Component({
  selector: 'app-menu-form',
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDialogModule,
    ButtonComponent,
  ],
  template: `
    <h2 mat-dialog-title>{{ title() }}</h2>
    <form [formGroup]="form" mat-dialog-content class="form">
      @if (data.mode === 'category') {
        <mat-form-field appearance="outline">
          <mat-label>Category name</mat-label>
          <input matInput formControlName="name" />
        </mat-form-field>
        <mat-form-field appearance="outline">
          <mat-label>Description</mat-label>
          <textarea matInput rows="2" formControlName="description"></textarea>
        </mat-form-field>
        <mat-form-field appearance="outline">
          <mat-label>Sort order</mat-label>
          <input matInput type="number" formControlName="sortOrder" />
        </mat-form-field>
      } @else {
        <mat-form-field appearance="outline">
          <mat-label>Category</mat-label>
          <mat-select formControlName="categoryId">
            @for (category of data.categories; track category.id) {
              <mat-option [value]="category.id">{{ category.name }}</mat-option>
            }
          </mat-select>
        </mat-form-field>
        <mat-form-field appearance="outline">
          <mat-label>Item name</mat-label>
          <input matInput formControlName="name" />
        </mat-form-field>
        <mat-form-field appearance="outline">
          <mat-label>Description</mat-label>
          <textarea matInput rows="2" formControlName="description"></textarea>
        </mat-form-field>
        <div class="row">
          <mat-form-field appearance="outline">
            <mat-label>Price</mat-label>
            <input matInput type="number" step="0.01" formControlName="price" />
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Prep time (min)</mat-label>
            <input matInput type="number" formControlName="preparationTime" />
          </mat-form-field>
        </div>
      }
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
      min-width: min(420px, 90vw);
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
export class MenuFormComponent {
  private readonly fb = inject(FormBuilder);
  private readonly menuService = inject(MenuService);
  readonly dialogRef = inject(MatDialogRef<MenuFormComponent>);
  readonly data = inject<MenuFormData>(MAT_DIALOG_DATA);

  readonly saving = signal(false);
  readonly error = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    name: ['', Validators.required],
    description: [''],
    sortOrder: [0],
    categoryId: [''],
    price: [0, Validators.min(0)],
    preparationTime: [15],
  });

  constructor() {
    if (this.data.mode === 'item') {
      this.form.controls.categoryId.setValidators([Validators.required]);
      this.form.controls.price.setValidators([Validators.required, Validators.min(0.01)]);
    }
    const category = this.data.category;
    const item = this.data.item;
    if (category) {
      this.form.patchValue({
        name: category.name,
        description: category.description ?? '',
        sortOrder: category.sortOrder,
      });
    }
    if (item) {
      this.form.patchValue({
        categoryId: item.categoryId,
        name: item.name,
        description: item.description ?? '',
        price: item.price,
        preparationTime: item.preparationTime ?? 15,
      });
    } else if (this.data.categories.length && this.data.mode === 'item') {
      this.form.patchValue({ categoryId: this.data.categories[0].id });
    }
  }

  title(): string {
    if (this.data.mode === 'category') {
      return this.data.category ? 'Edit category' : 'Add category';
    }
    return this.data.item ? 'Edit menu item' : 'Add menu item';
  }

  submit(): void {
    if (this.form.invalid) return;
    this.saving.set(true);
    this.error.set(null);
    const value = this.form.getRawValue();

    if (this.data.mode === 'category') {
      if (this.data.category) {
        const request: UpdateCategoryRequest = {
          name: value.name,
          description: value.description || undefined,
          sortOrder: value.sortOrder,
        };
        this.menuService.updateCategory(this.data.category.id, request).subscribe({
          next: () => this.dialogRef.close(true),
          error: () => this.fail('Failed to update category.'),
        });
      } else {
        const request: CreateCategoryRequest = {
          restaurantId: this.data.restaurantId,
          name: value.name,
          description: value.description || undefined,
          sortOrder: value.sortOrder,
        };
        this.menuService.createCategory(request).subscribe({
          next: () => this.dialogRef.close(true),
          error: () => this.fail('Failed to create category.'),
        });
      }
      return;
    }

    if (this.data.item) {
      const request: UpdateMenuItemRequest = {
        categoryId: value.categoryId,
        name: value.name,
        description: value.description || undefined,
        price: value.price,
        preparationTime: value.preparationTime,
      };
      this.menuService.updateItem(this.data.item.id, request, this.data.item.version).subscribe({
        next: () => this.dialogRef.close(true),
        error: () => this.fail('Failed to update menu item.'),
      });
    } else {
      const request: CreateMenuItemRequest = {
        categoryId: value.categoryId,
        name: value.name,
        description: value.description || undefined,
        price: value.price,
        preparationTime: value.preparationTime,
      };
      this.menuService.createItem(request).subscribe({
        next: () => this.dialogRef.close(true),
        error: () => this.fail('Failed to create menu item.'),
      });
    }
  }

  private fail(message: string): void {
    this.error.set(message);
    this.saving.set(false);
  }
}
