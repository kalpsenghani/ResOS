import { CurrencyPipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { MatTabsModule } from '@angular/material/tabs';
import { forkJoin } from 'rxjs';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { CardComponent } from '../../../shared/components/card/card.component';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';
import { KpiWidgetComponent } from '../../../shared/components/kpi-widget/kpi-widget.component';
import { RevenueChartComponent } from '../../dashboard/components/revenue-chart/revenue-chart.component';
import { MotionStaggerDirective } from '../../../shared/animations';
import { AnalyticsService } from '../services/analytics.service';
import { RestaurantService } from '../../../core/restaurant/restaurant.service';
import {
  EmployeeAnalytics,
  InventoryAnalytics,
  OrderAnalytics,
  RevenueAnalytics,
} from '../../../shared/models/analytics.model';
import { RevenueChart } from '../../../shared/models/dashboard.model';

@Component({
  selector: 'app-analytics-dashboard',
  imports: [
    MatTabsModule,
    PageHeaderComponent,
    CardComponent,
    LoadingSpinnerComponent,
    EmptyStateComponent,
    KpiWidgetComponent,
    RevenueChartComponent,
    MotionStaggerDirective,
    CurrencyPipe,
  ],
  templateUrl: './analytics-dashboard.component.html',
  styleUrl: './analytics-dashboard.component.scss',
})
export class AnalyticsDashboardComponent implements OnInit {
  private readonly analyticsService = inject(AnalyticsService);
  private readonly restaurantService = inject(RestaurantService);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly restaurantId = signal<string | null>(null);
  readonly revenue = signal<RevenueAnalytics | null>(null);
  readonly inventory = signal<InventoryAnalytics | null>(null);
  readonly employees = signal<EmployeeAnalytics | null>(null);
  readonly orders = signal<OrderAnalytics | null>(null);

  readonly revenueChart = signal<RevenueChart>({ labels: [], values: [] });

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
        this.loadAnalytics(restaurant.id);
      },
      error: () => {
        this.error.set('Failed to load restaurant.');
        this.loading.set(false);
      },
    });
  }

  loadAnalytics(restaurantId: string): void {
    this.loading.set(true);
    const endDate = new Date().toISOString().slice(0, 10);
    const startDate = new Date(Date.now() - 29 * 86400000).toISOString().slice(0, 10);

    forkJoin({
      revenue: this.analyticsService.getRevenue({ restaurantId, startDate, endDate }),
      inventory: this.analyticsService.getInventory(restaurantId),
      employees: this.analyticsService.getEmployees({ restaurantId, startDate, endDate }),
      orders: this.analyticsService.getOrders({ restaurantId, startDate, endDate }),
    }).subscribe({
      next: ({ revenue, inventory, employees, orders }) => {
        this.revenue.set(revenue.data);
        this.inventory.set(inventory.data);
        this.employees.set(employees.data);
        this.orders.set(orders.data);
        this.revenueChart.set({
          labels: revenue.data.labels,
          values: revenue.data.values,
        });
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Failed to load analytics.');
        this.loading.set(false);
      },
    });
  }

  formatHour(hour: number): string {
    const suffix = hour >= 12 ? 'PM' : 'AM';
    const normalized = hour % 12 === 0 ? 12 : hour % 12;
    return `${normalized} ${suffix}`;
  }
}
