import { CurrencyPipe, DecimalPipe } from '@angular/common';
import { Component, computed, input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { KpiTrend } from '../../models/dashboard.model';

@Component({
  selector: 'app-kpi-widget',
  imports: [MatIconModule, CurrencyPipe, DecimalPipe],
  template: `
    <article class="kpi">
      <div class="kpi__header">
        <span class="kpi__label">{{ label() }}</span>
        @if (icon()) {
          <mat-icon class="kpi__icon">{{ icon() }}</mat-icon>
        }
      </div>
      <p class="kpi__value">
        @if (format() === 'currency') {
          {{ value() | currency: 'USD' : 'symbol' : '1.0-0' }}
        } @else {
          {{ value() | number: '1.0-0' }}
        }
      </p>
      <div class="kpi__trend" [class]="trendClass()">
        <mat-icon>{{ trendIcon() }}</mat-icon>
        <span>{{ changeLabel() }}</span>
      </div>
    </article>
  `,
  styles: `
    .kpi {
      background: var(--surface-card);
      border: 1px solid var(--border-subtle);
      border-radius: 0.75rem;
      padding: 1.25rem;
      box-shadow: var(--shadow-sm);
    }

    .kpi__header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 0.75rem;
    }

    .kpi__label {
      font-size: 0.875rem;
      color: var(--text-muted);
      font-weight: 500;
    }

    .kpi__icon {
      color: var(--brand-primary);
      font-size: 1.25rem;
      width: 1.25rem;
      height: 1.25rem;
    }

    .kpi__value {
      margin: 0;
      font-size: 1.75rem;
      font-weight: 700;
      letter-spacing: -0.02em;
      color: var(--text-primary);
    }

    .kpi__trend {
      display: inline-flex;
      align-items: center;
      gap: 0.25rem;
      margin-top: 0.75rem;
      font-size: 0.8125rem;
      font-weight: 500;
    }

    .kpi__trend mat-icon {
      font-size: 1rem;
      width: 1rem;
      height: 1rem;
    }

    .kpi__trend--up {
      color: var(--color-success);
    }

    .kpi__trend--down {
      color: var(--color-danger);
    }

    .kpi__trend--flat {
      color: var(--text-muted);
    }
  `,
})
export class KpiWidgetComponent {
  readonly label = input.required<string>();
  readonly value = input.required<number>();
  readonly change = input(0);
  readonly trend = input<KpiTrend>('FLAT');
  readonly icon = input<string | undefined>(undefined);
  readonly format = input<'number' | 'currency'>('number');

  readonly trendClass = computed(() => {
    const trend = this.trend();
    if (trend === 'UP') return 'kpi__trend kpi__trend--up';
    if (trend === 'DOWN') return 'kpi__trend kpi__trend--down';
    return 'kpi__trend kpi__trend--flat';
  });

  readonly trendIcon = computed(() => {
    const trend = this.trend();
    if (trend === 'UP') return 'trending_up';
    if (trend === 'DOWN') return 'trending_down';
    return 'trending_flat';
  });

  readonly changeLabel = computed(() => {
    const change = this.change();
    const prefix = change > 0 ? '+' : '';
    return `${prefix}${change.toFixed(1)}% vs last period`;
  });
}
