import { Component, inject, OnInit, signal } from '@angular/core';
import { forkJoin } from 'rxjs';
import { Store } from '@ngrx/store';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';
import { KpiWidgetComponent } from '../../shared/components/kpi-widget/kpi-widget.component';
import { CardComponent } from '../../shared/components/card/card.component';
import { DataTableComponent } from '../../shared/components/data-table/data-table.component';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';
import { RevenueChartComponent } from './components/revenue-chart/revenue-chart.component';
import { MotionStaggerDirective } from '../../shared/animations';
import { DashboardService } from '../../core/dashboard/dashboard.service';
import { RestaurantService } from '../../core/restaurant/restaurant.service';
import { DashboardKpis, RecentOrder, RevenueChart } from '../../shared/models/dashboard.model';
import { TenantActions } from '../../store/tenant/tenant.actions';

@Component({
  selector: 'app-dashboard',
  imports: [
    PageHeaderComponent,
    KpiWidgetComponent,
    CardComponent,
    DataTableComponent,
    LoadingSpinnerComponent,
    EmptyStateComponent,
    RevenueChartComponent,
    MotionStaggerDirective,
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
})
export class DashboardComponent implements OnInit {
  private readonly dashboardService = inject(DashboardService);
  private readonly restaurantService = inject(RestaurantService);
  private readonly store = inject(Store);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly restaurantName = signal('');
  readonly kpis = signal<DashboardKpis | null>(null);
  readonly recentOrders = signal<RecentOrder[]>([]);
  readonly revenueChart = signal<RevenueChart>({ labels: [], values: [] });

  readonly orderColumns = [
    { key: 'orderNumber' as const, label: 'Order #' },
    { key: 'customerName' as const, label: 'Customer' },
    { key: 'status' as const, label: 'Status' },
    { key: 'totalAmount' as const, label: 'Total', format: 'currency' as const },
    { key: 'createdAt' as const, label: 'Date', format: 'date' as const },
  ];

  ngOnInit(): void {
    this.store.dispatch(TenantActions.loadCurrent());
    this.loadDashboard();
  }

  loadDashboard(): void {
    this.loading.set(true);
    this.error.set(null);

    this.restaurantService.list().subscribe({
      next: (response) => {
        const restaurants = response.data;
        if (!restaurants.length) {
          this.loading.set(false);
          this.error.set('No restaurant found. Complete registration to create your default location.');
          return;
        }

        const restaurant = restaurants[0];
        this.restaurantName.set(restaurant.name);

        forkJoin({
          kpis: this.dashboardService.getKpis(restaurant.id),
          orders: this.dashboardService.getRecentOrders(restaurant.id),
          chart: this.dashboardService.getRevenueChart(restaurant.id),
        }).subscribe({
          next: ({ kpis, orders, chart }) => {
            this.kpis.set(kpis.data);
            this.recentOrders.set(orders.data);
            this.revenueChart.set(chart.data);
            this.loading.set(false);
          },
          error: () => {
            this.error.set('Failed to load dashboard data.');
            this.loading.set(false);
          },
        });
      },
      error: () => {
        this.error.set('Failed to load restaurant data.');
        this.loading.set(false);
      },
    });
  }
}
