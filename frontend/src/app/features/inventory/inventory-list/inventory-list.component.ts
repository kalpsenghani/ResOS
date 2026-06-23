import { DecimalPipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatTabsModule } from '@angular/material/tabs';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { CardComponent } from '../../../shared/components/card/card.component';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { ButtonComponent } from '../../../shared/components/button/button.component';
import { InventoryService } from '../services/inventory.service';
import { RestaurantService } from '../../../core/restaurant/restaurant.service';
import { InventoryItem, StockAlert } from '../../../shared/models/inventory.model';
import { InventoryFormComponent } from '../inventory-form/inventory-form.component';

@Component({
  selector: 'app-inventory-list',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatCheckboxModule,
    MatTabsModule,
    PageHeaderComponent,
    CardComponent,
    LoadingSpinnerComponent,
    EmptyStateComponent,
    StatusBadgeComponent,
    ButtonComponent,
    DecimalPipe,
  ],
  templateUrl: './inventory-list.component.html',
  styleUrl: './inventory-list.component.scss',
})
export class InventoryListComponent implements OnInit {
  private readonly inventoryService = inject(InventoryService);
  private readonly restaurantService = inject(RestaurantService);
  private readonly dialog = inject(MatDialog);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly items = signal<InventoryItem[]>([]);
  readonly alerts = signal<StockAlert[]>([]);
  readonly restaurantId = signal<string | null>(null);

  readonly searchControl = new FormControl('', { nonNullable: true });
  readonly lowStockOnly = signal(false);

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
        this.loadData();
      },
      error: () => {
        this.error.set('Failed to load restaurant.');
        this.loading.set(false);
      },
    });

    this.searchControl.valueChanges.pipe(debounceTime(300), distinctUntilChanged()).subscribe(() => {
      if (this.restaurantId()) this.loadItems();
    });
  }

  loadData(): void {
    this.loadItems();
    this.loadAlerts();
  }

  loadItems(): void {
    const restaurantId = this.restaurantId();
    if (!restaurantId) return;

    this.loading.set(true);
    this.inventoryService
      .list({
        restaurantId,
        lowStock: this.lowStockOnly() || undefined,
        search: this.searchControl.value || undefined,
      })
      .subscribe({
        next: (response) => {
          this.items.set(response.data);
          this.loading.set(false);
        },
        error: () => {
          this.error.set('Failed to load inventory.');
          this.loading.set(false);
        },
      });
  }

  loadAlerts(): void {
    const restaurantId = this.restaurantId();
    if (!restaurantId) return;

    this.inventoryService.listAlerts(restaurantId, false).subscribe({
      next: (response) => this.alerts.set(response.data),
    });
  }

  toggleLowStock(checked: boolean): void {
    this.lowStockOnly.set(checked);
    this.loadItems();
  }

  openCreateDialog(): void {
    const restaurantId = this.restaurantId();
    if (!restaurantId) return;

    this.dialog
      .open(InventoryFormComponent, { data: { restaurantId } })
      .afterClosed()
      .subscribe((saved) => {
        if (saved) this.loadData();
      });
  }

  openEditDialog(item: InventoryItem): void {
    const restaurantId = this.restaurantId();
    if (!restaurantId) return;

    this.dialog
      .open(InventoryFormComponent, { data: { restaurantId, item } })
      .afterClosed()
      .subscribe((saved) => {
        if (saved) this.loadData();
      });
  }

  acknowledgeAlert(alert: StockAlert): void {
    this.inventoryService.acknowledgeAlert(alert.id).subscribe({
      next: () => this.loadAlerts(),
    });
  }

  alertVariant(type: string): 'success' | 'warning' | 'danger' | 'info' | 'neutral' {
    if (type === 'OUT_OF_STOCK') return 'danger';
    if (type === 'LOW_STOCK') return 'warning';
    return 'info';
  }
}
