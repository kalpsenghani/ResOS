import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatTabsModule } from '@angular/material/tabs';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { CardComponent } from '../../../shared/components/card/card.component';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { ButtonComponent } from '../../../shared/components/button/button.component';
import { MotionStaggerDirective } from '../../../shared/animations';
import { OrderService } from '../services/order.service';
import { MenuService } from '../../menu/services/menu.service';
import { RestaurantService } from '../../../core/restaurant/restaurant.service';
import { Order, OrderItemStatus, OrderStatus } from '../../../shared/models/order.model';
import { MenuItem } from '../../../shared/models/menu.model';
import { OrderFormComponent } from '../order-form/order-form.component';

@Component({
  selector: 'app-order-list',
  imports: [
    MatDialogModule,
    MatTabsModule,
    PageHeaderComponent,
    CardComponent,
    LoadingSpinnerComponent,
    EmptyStateComponent,
    StatusBadgeComponent,
    ButtonComponent,
    MotionStaggerDirective,
    CurrencyPipe,
    DatePipe,
  ],
  templateUrl: './order-list.component.html',
  styleUrl: './order-list.component.scss',
})
export class OrderListComponent implements OnInit {
  private readonly orderService = inject(OrderService);
  private readonly menuService = inject(MenuService);
  private readonly restaurantService = inject(RestaurantService);
  private readonly dialog = inject(MatDialog);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly orders = signal<Order[]>([]);
  readonly menuItems = signal<MenuItem[]>([]);
  readonly restaurantId = signal<string | null>(null);

  readonly kitchenOrders = computed(() =>
    this.orders().filter((order) => ['CONFIRMED', 'PREPARING', 'READY'].includes(order.status)),
  );

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
        this.loadAll();
      },
      error: () => {
        this.error.set('Failed to load restaurant.');
        this.loading.set(false);
      },
    });
  }

  loadAll(): void {
    const restaurantId = this.restaurantId();
    if (!restaurantId) return;

    this.loading.set(true);
    this.orderService.list({ restaurantId, size: 100 }).subscribe({
      next: (response) => {
        this.orders.set(response.data);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Failed to load orders.');
        this.loading.set(false);
      },
    });

    this.menuService.listItems({ restaurantId, available: true }).subscribe({
      next: (response) => this.menuItems.set(response.data),
    });
  }

  openCreateDialog(): void {
    const restaurantId = this.restaurantId();
    if (!restaurantId) return;

    this.dialog
      .open(OrderFormComponent, { data: { restaurantId, menuItems: this.menuItems() } })
      .afterClosed()
      .subscribe((saved) => {
        if (saved) this.loadAll();
      });
  }

  advanceOrder(order: Order, status: OrderStatus): void {
    this.orderService.updateStatus(order.id, status).subscribe({
      next: () => this.loadAll(),
    });
  }

  advanceItem(order: Order, itemId: string, status: OrderItemStatus): void {
    this.orderService.updateItemStatus(order.id, itemId, status).subscribe({
      next: () => this.loadAll(),
    });
  }

  statusVariant(status: string): 'success' | 'warning' | 'danger' | 'neutral' {
    if (status === 'COMPLETED' || status === 'SERVED' || status === 'READY') return 'success';
    if (status === 'PREPARING' || status === 'CONFIRMED' || status === 'PENDING') return 'warning';
    if (status === 'CANCELLED') return 'danger';
    return 'neutral';
  }

  nextItemStatus(status: OrderItemStatus): OrderItemStatus | null {
    if (status === 'PENDING') return 'PREPARING';
    if (status === 'PREPARING') return 'READY';
    if (status === 'READY') return 'SERVED';
    return null;
  }
}
