import { CurrencyPipe, DecimalPipe } from '@angular/common';
import { Component, computed, effect, input, signal, untracked } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { animate } from 'motion';
import { prefersReducedMotion } from '../../animations/motion-presets';
import { KpiTrend } from '../../models/dashboard.model';

@Component({
  selector: 'app-kpi-widget',
  imports: [MatIconModule, CurrencyPipe, DecimalPipe],
  template: `
    <article class="kpi">
      <div class="kpi__top">
        <span class="kpi__icon-wrap">
          <mat-icon class="kpi__icon">{{ icon() || 'insights' }}</mat-icon>
        </span>
        <span class="kpi__trend" [class]="trendClass()">
          <mat-icon>{{ trendIcon() }}</mat-icon>
          {{ changeValue() }}
        </span>
      </div>
      <p class="kpi__value">
        @if (format() === 'currency') {
          {{ displayValue() | currency: 'USD' : 'symbol' : '1.0-0' }}
        } @else {
          {{ displayValue() | number: '1.0-0' }}
        }
      </p>
      <span class="kpi__label">{{ label() }}</span>
    </article>
  `,
  styles: `
    .kpi {
      position: relative;
      background: var(--surface-card);
      border: 1px solid var(--border-subtle);
      border-radius: 1.1rem;
      padding: 1.15rem;
      box-shadow: var(--shadow-sm);
      overflow: hidden;
      transition: box-shadow 0.25s ease, transform 0.25s ease, border-color 0.25s ease;
    }
    .kpi::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 3px;
      background: linear-gradient(90deg, var(--brand-primary), var(--accent));
      opacity: 0;
      transition: opacity 0.25s ease;
    }
    .kpi:hover {
      box-shadow: var(--shadow-md);
      transform: translateY(-3px);
      border-color: var(--border-strong);
    }
    .kpi:hover::before { opacity: 1; }

    .kpi__top {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 0.75rem;
    }
    .kpi__icon-wrap {
      display: grid;
      place-items: center;
      width: 2.5rem;
      height: 2.5rem;
      border-radius: 0.85rem;
      background: var(--brand-primary-subtle);
    }
    .kpi__icon {
      color: var(--brand-primary);
      font-size: 1.3rem;
      width: 1.3rem;
      height: 1.3rem;
    }
    .kpi__trend {
      display: inline-flex;
      align-items: center;
      gap: 0.15rem;
      font-size: 0.78rem;
      font-weight: 700;
      padding: 0.2rem 0.5rem;
      border-radius: 999px;
    }
    .kpi__trend mat-icon {
      font-size: 0.95rem;
      width: 0.95rem;
      height: 0.95rem;
    }
    .kpi__trend--up { color: var(--color-success); background: var(--color-success-subtle); }
    .kpi__trend--down { color: var(--color-danger); background: var(--color-danger-subtle); }
    .kpi__trend--flat { color: var(--text-muted); background: var(--surface-hover); }

    .kpi__value {
      margin: 0;
      font-size: 1.65rem;
      font-weight: 800;
      letter-spacing: -0.03em;
      color: var(--text-primary);
      line-height: 1.1;
    }
    .kpi__label {
      display: block;
      margin-top: 0.3rem;
      font-size: 0.85rem;
      font-weight: 500;
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

  /** Smoothly counts up to the latest value for a premium dashboard feel. */
  readonly displayValue = signal(0);

  constructor() {
    effect((onCleanup) => {
      const target = this.value();
      if (prefersReducedMotion()) {
        this.displayValue.set(target);
        return;
      }
      const from = untracked(this.displayValue);
      const controls = animate(from, target, {
        duration: 0.9,
        ease: [0.22, 1, 0.36, 1],
        onUpdate: (latest) => this.displayValue.set(latest),
      });
      onCleanup(() => controls.stop());
    });
  }

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

  readonly changeValue = computed(() => {
    const change = this.change();
    const prefix = change > 0 ? '+' : '';
    return `${prefix}${change.toFixed(1)}%`;
  });
}
