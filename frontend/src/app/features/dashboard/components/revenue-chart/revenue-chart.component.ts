import { Component, computed, inject, input, signal } from '@angular/core';
import { RevenueChart } from '../../../../shared/models/dashboard.model';

@Component({
  selector: 'app-revenue-chart',
  template: `
    <div class="chart">
      @if (chart().values.length) {
        <div class="chart__bars">
          @for (value of chart().values; track $index) {
            <div class="chart__bar-group">
              <div
                class="chart__bar"
                [style.height.%]="barHeight(value)"
                [attr.aria-label]="chart().labels[$index] + ': ' + value"
              ></div>
              <span class="chart__label">{{ chart().labels[$index] }}</span>
            </div>
          }
        </div>
      } @else {
        <p class="chart__empty">No revenue data for this period yet.</p>
      }
    </div>
  `,
  styles: `
    .chart {
      min-height: 220px;
    }

    .chart__bars {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(2.5rem, 1fr));
      align-items: end;
      gap: 0.75rem;
      height: 220px;
      padding-top: 1rem;
    }

    .chart__bar-group {
      display: flex;
      flex-direction: column;
      align-items: center;
      height: 100%;
      justify-content: flex-end;
      gap: 0.5rem;
    }

    .chart__bar {
      width: 100%;
      max-width: 2.5rem;
      min-height: 4px;
      border-radius: 0.375rem 0.375rem 0 0;
      background: linear-gradient(180deg, var(--brand-primary) 0%, var(--brand-primary-hover) 100%);
      transition: height 0.3s ease;
    }

    .chart__label {
      font-size: 0.75rem;
      color: var(--text-muted);
    }

    .chart__empty {
      display: grid;
      place-items: center;
      min-height: 220px;
      margin: 0;
      color: var(--text-muted);
    }
  `,
})
export class RevenueChartComponent {
  readonly chart = input.required<RevenueChart>();

  readonly maxValue = computed(() => {
    const values = this.chart().values;
    return values.length ? Math.max(...values) : 1;
  });

  barHeight(value: number): number {
    const max = this.maxValue();
    return max > 0 ? Math.max((value / max) * 100, 4) : 4;
  }
}
