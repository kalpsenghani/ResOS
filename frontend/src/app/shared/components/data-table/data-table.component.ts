import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, input } from '@angular/core';

export interface DataTableColumn<T> {
  key: keyof T & string;
  label: string;
  format?: 'text' | 'currency' | 'date';
}

@Component({
  selector: 'app-data-table',
  imports: [DatePipe, CurrencyPipe],
  template: `
    <div class="table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            @for (column of columns(); track column.key) {
              <th>{{ column.label }}</th>
            }
          </tr>
        </thead>
        <tbody>
          @for (row of rows(); track trackBy(row)) {
            <tr>
              @for (column of columns(); track column.key) {
                <td>
                  @if (column.format === 'currency') {
                    {{ asNumber(row[column.key]) | currency: 'USD' }}
                  } @else if (column.format === 'date') {
                    {{ asString(row[column.key]) | date: 'short' }}
                  } @else {
                    {{ row[column.key] }}
                  }
                </td>
              }
            </tr>
          } @empty {
            <tr>
              <td [attr.colspan]="columns().length" class="empty-cell">
                {{ emptyMessage() }}
              </td>
            </tr>
          }
        </tbody>
      </table>
    </div>
  `,
  styles: `
    .table-wrap {
      overflow-x: auto;
    }

    .data-table {
      width: 100%;
      border-collapse: collapse;
      font-size: 0.875rem;
    }

    th,
    td {
      padding: 0.875rem 1rem;
      text-align: left;
      border-bottom: 1px solid var(--border-subtle);
    }

    th {
      color: var(--text-muted);
      font-weight: 600;
      font-size: 0.75rem;
      text-transform: uppercase;
      letter-spacing: 0.04em;
    }

    tbody tr:hover {
      background: var(--brand-primary-subtle);
    }

    .empty-cell {
      text-align: center;
      color: var(--text-muted);
      padding: 2rem 1rem;
    }
  `,
})
export class DataTableComponent<T extends Record<string, unknown>> {
  readonly columns = input.required<DataTableColumn<T>[]>();
  readonly rows = input.required<T[]>();
  readonly emptyMessage = input('No data available');

  trackBy(row: T): string {
    return String(row['id'] ?? JSON.stringify(row));
  }

  asNumber(value: unknown): number {
    return Number(value ?? 0);
  }

  asString(value: unknown): string {
    return String(value ?? '');
  }
}
