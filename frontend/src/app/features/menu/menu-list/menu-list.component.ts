import { CurrencyPipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatTabsModule } from '@angular/material/tabs';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { CardComponent } from '../../../shared/components/card/card.component';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { ButtonComponent } from '../../../shared/components/button/button.component';
import { MotionStaggerDirective } from '../../../shared/animations';
import { MenuService } from '../services/menu.service';
import { RestaurantService } from '../../../core/restaurant/restaurant.service';
import { MenuCategory, MenuItem } from '../../../shared/models/menu.model';
import { MenuFormComponent } from '../menu-form/menu-form.component';

@Component({
  selector: 'app-menu-list',
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
  ],
  templateUrl: './menu-list.component.html',
  styleUrl: './menu-list.component.scss',
})
export class MenuListComponent implements OnInit {
  private readonly menuService = inject(MenuService);
  private readonly restaurantService = inject(RestaurantService);
  private readonly dialog = inject(MatDialog);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly categories = signal<MenuCategory[]>([]);
  readonly items = signal<MenuItem[]>([]);
  readonly restaurantId = signal<string | null>(null);

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
    this.menuService.listCategories(restaurantId).subscribe({
      next: (categoryResponse) => {
        this.categories.set(categoryResponse.data);
        this.menuService.listItems({ restaurantId }).subscribe({
          next: (itemResponse) => {
            this.items.set(itemResponse.data);
            this.loading.set(false);
          },
          error: () => {
            this.error.set('Failed to load menu items.');
            this.loading.set(false);
          },
        });
      },
      error: () => {
        this.error.set('Failed to load categories.');
        this.loading.set(false);
      },
    });
  }

  openCategoryDialog(category?: MenuCategory): void {
    const restaurantId = this.restaurantId();
    if (!restaurantId) return;

    this.dialog
      .open(MenuFormComponent, { data: { mode: 'category', restaurantId, categories: this.categories(), category } })
      .afterClosed()
      .subscribe((saved) => {
        if (saved) this.loadAll();
      });
  }

  openItemDialog(item?: MenuItem): void {
    const restaurantId = this.restaurantId();
    if (!restaurantId) return;

    this.dialog
      .open(MenuFormComponent, {
        data: { mode: 'item', restaurantId, categories: this.categories(), item },
      })
      .afterClosed()
      .subscribe((saved) => {
        if (saved) this.loadAll();
      });
  }

  toggleAvailability(item: MenuItem): void {
    this.menuService.setAvailability(item.id, !item.available).subscribe({
      next: () => this.loadAll(),
    });
  }

  deleteCategory(category: MenuCategory): void {
    this.menuService.deleteCategory(category.id).subscribe({
      next: () => this.loadAll(),
    });
  }

  deleteItem(item: MenuItem): void {
    this.menuService.deleteItem(item.id).subscribe({
      next: () => this.loadAll(),
    });
  }

  categoryName(categoryId: string): string {
    return this.categories().find((category) => category.id === categoryId)?.name ?? '—';
  }
}
