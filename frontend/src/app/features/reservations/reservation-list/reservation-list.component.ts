import { DatePipe } from '@angular/common';
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
import { ReservationService } from '../services/reservation.service';
import { RestaurantService } from '../../../core/restaurant/restaurant.service';
import { Reservation, ReservationStatus, RestaurantTable } from '../../../shared/models/reservation.model';
import { ReservationFormComponent } from '../reservation-form/reservation-form.component';
import { TableFormComponent } from '../table-form/table-form.component';

@Component({
  selector: 'app-reservation-list',
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
    DatePipe,
  ],
  templateUrl: './reservation-list.component.html',
  styleUrl: './reservation-list.component.scss',
})
export class ReservationListComponent implements OnInit {
  private readonly reservationService = inject(ReservationService);
  private readonly restaurantService = inject(RestaurantService);
  private readonly dialog = inject(MatDialog);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly reservations = signal<Reservation[]>([]);
  readonly tables = signal<RestaurantTable[]>([]);
  readonly restaurantId = signal<string | null>(null);
  readonly weekStart = signal(this.startOfWeek(new Date()));

  readonly weekDays = computed(() => {
    const start = this.weekStart();
    return Array.from({ length: 7 }, (_, index) => {
      const date = new Date(start);
      date.setDate(start.getDate() + index);
      return date;
    });
  });

  readonly calendarReservations = computed(() => {
    const map = new Map<string, Reservation[]>();
    for (const reservation of this.reservations()) {
      const key = reservation.reservationDate;
      const list = map.get(key) ?? [];
      list.push(reservation);
      map.set(key, list);
    }
    return map;
  });

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
    this.loadReservations();
    this.loadTables();
  }

  loadReservations(): void {
    const restaurantId = this.restaurantId();
    if (!restaurantId) return;

    this.loading.set(true);
    const start = this.weekStart();
    const end = new Date(start);
    end.setDate(start.getDate() + 6);

    this.reservationService
      .listReservations({
        restaurantId,
        startDate: this.formatDate(start),
        endDate: this.formatDate(end),
        size: 200,
      })
      .subscribe({
        next: (response) => {
          this.reservations.set(response.data);
          this.loading.set(false);
        },
        error: () => {
          this.error.set('Failed to load reservations.');
          this.loading.set(false);
        },
      });
  }

  loadTables(): void {
    const restaurantId = this.restaurantId();
    if (!restaurantId) return;

    this.reservationService.listTables(restaurantId).subscribe({
      next: (response) => this.tables.set(response.data),
    });
  }

  previousWeek(): void {
    const start = new Date(this.weekStart());
    start.setDate(start.getDate() - 7);
    this.weekStart.set(start);
    this.loadReservations();
  }

  nextWeek(): void {
    const start = new Date(this.weekStart());
    start.setDate(start.getDate() + 7);
    this.weekStart.set(start);
    this.loadReservations();
  }

  openCreateReservationDialog(): void {
    const restaurantId = this.restaurantId();
    if (!restaurantId) return;

    this.dialog
      .open(ReservationFormComponent, { data: { restaurantId, tables: this.tables() } })
      .afterClosed()
      .subscribe((saved) => {
        if (saved) this.loadAll();
      });
  }

  openEditReservationDialog(reservation: Reservation): void {
    const restaurantId = this.restaurantId();
    if (!restaurantId) return;

    this.dialog
      .open(ReservationFormComponent, {
        data: { restaurantId, tables: this.tables(), reservation },
      })
      .afterClosed()
      .subscribe((saved) => {
        if (saved) this.loadAll();
      });
  }

  openCreateTableDialog(): void {
    const restaurantId = this.restaurantId();
    if (!restaurantId) return;

    this.dialog
      .open(TableFormComponent, { data: { restaurantId } })
      .afterClosed()
      .subscribe((saved) => {
        if (saved) this.loadTables();
      });
  }

  openEditTableDialog(table: RestaurantTable): void {
    const restaurantId = this.restaurantId();
    if (!restaurantId) return;

    this.dialog
      .open(TableFormComponent, { data: { restaurantId, table } })
      .afterClosed()
      .subscribe((saved) => {
        if (saved) this.loadTables();
      });
  }

  updateStatus(reservation: Reservation, status: ReservationStatus): void {
    this.reservationService.updateStatus(reservation.id, status).subscribe({
      next: () => this.loadReservations(),
    });
  }

  cancelReservation(reservation: Reservation): void {
    this.reservationService.cancel(reservation.id).subscribe({
      next: () => this.loadReservations(),
    });
  }

  deleteTable(table: RestaurantTable): void {
    this.reservationService.deleteTable(table.id).subscribe({
      next: () => this.loadTables(),
    });
  }

  reservationsForDay(date: Date): Reservation[] {
    return this.calendarReservations().get(this.formatDate(date)) ?? [];
  }

  statusVariant(status: string): 'success' | 'warning' | 'danger' | 'neutral' {
    if (status === 'CONFIRMED' || status === 'SEATED' || status === 'COMPLETED') return 'success';
    if (status === 'PENDING') return 'warning';
    if (status === 'CANCELLED' || status === 'NO_SHOW') return 'danger';
    return 'neutral';
  }

  formatDayTitle(date: Date): string {
    return date.toLocaleDateString(undefined, { weekday: 'short', month: 'short', day: 'numeric' });
  }

  private startOfWeek(date: Date): Date {
    const start = new Date(date);
    const day = start.getDay();
    const diff = day === 0 ? -6 : 1 - day;
    start.setDate(start.getDate() + diff);
    start.setHours(0, 0, 0, 0);
    return start;
  }

  private formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
