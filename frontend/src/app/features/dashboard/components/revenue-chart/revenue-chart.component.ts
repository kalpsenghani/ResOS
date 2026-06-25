import { Component, computed, input } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { RevenueChart } from '../../../../shared/models/dashboard.model';

interface ChartPoint {
  x: number;
  y: number;
  value: number;
  label: string;
}

const W = 560;
const H = 220;
const PAD_X = 14;
const PAD_TOP = 18;
const PAD_BOTTOM = 32;

@Component({
  selector: 'app-revenue-chart',
  imports: [CurrencyPipe],
  template: `
    @if (points().length) {
      <div class="chart">
        <svg
          class="chart__svg"
          [attr.viewBox]="viewBox"
          preserveAspectRatio="none"
          role="img"
          aria-label="Revenue trend"
        >
          <defs>
            <linearGradient id="revArea" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stop-color="var(--brand-primary)" stop-opacity="0.28" />
              <stop offset="100%" stop-color="var(--brand-primary)" stop-opacity="0" />
            </linearGradient>
            <linearGradient id="revLine" x1="0" y1="0" x2="1" y2="0">
              <stop offset="0%" stop-color="var(--brand-primary)" />
              <stop offset="100%" stop-color="var(--accent)" />
            </linearGradient>
          </defs>

          @for (gy of gridLines(); track gy) {
            <line class="chart__grid" [attr.x1]="padX" [attr.x2]="innerRight" [attr.y1]="gy" [attr.y2]="gy" />
          }

          <path class="chart__area" [attr.d]="areaPath()" fill="url(#revArea)" />
          <path
            class="chart__line"
            [attr.d]="linePath()"
            fill="none"
            stroke="url(#revLine)"
            stroke-width="3"
            stroke-linecap="round"
            stroke-linejoin="round"
            vector-effect="non-scaling-stroke"
          />

          @for (p of points(); track p.label) {
            <circle class="chart__dot" [attr.cx]="p.x" [attr.cy]="p.y" r="4" vector-effect="non-scaling-stroke" />
          }
        </svg>

        <div class="chart__labels">
          @for (p of points(); track p.label) {
            <span class="chart__label">{{ p.label }}</span>
          }
        </div>

        <div class="chart__peak">
          <span class="chart__peak-label">Peak</span>
          <span class="chart__peak-value">{{ peak() | currency: 'USD' : 'symbol' : '1.0-0' }}</span>
        </div>
      </div>
    } @else {
      <p class="chart__empty">No revenue data for this period yet.</p>
    }
  `,
  styles: `
    .chart {
      position: relative;
      width: 100%;
    }
    .chart__svg {
      width: 100%;
      height: 220px;
      display: block;
      overflow: visible;
    }
    .chart__grid {
      stroke: var(--border-subtle);
      stroke-width: 1;
      stroke-dasharray: 4 6;
      vector-effect: non-scaling-stroke;
    }
    .chart__area {
      opacity: 0;
      animation: area-in 0.6s ease 0.5s forwards;
    }
    @keyframes area-in {
      to { opacity: 1; }
    }
    .chart__line {
      stroke-dasharray: 1;
      stroke-dashoffset: 1;
      animation: draw 1.1s cubic-bezier(0.4, 0, 0.2, 1) forwards;
    }
    @keyframes draw {
      to { stroke-dashoffset: 0; }
    }
    .chart__dot {
      fill: var(--surface-card);
      stroke: var(--brand-primary);
      stroke-width: 3;
      opacity: 0;
      animation: dot-in 0.3s ease forwards;
      animation-delay: 1.1s;
      transition: r 0.2s ease;
    }
    .chart__dot:hover { r: 6; }
    @keyframes dot-in {
      from { opacity: 0; }
      to { opacity: 1; }
    }
    .chart__labels {
      display: flex;
      justify-content: space-between;
      margin-top: 0.5rem;
      padding: 0 0.25rem;
    }
    .chart__label {
      font-size: 0.72rem;
      font-weight: 600;
      color: var(--text-muted);
    }
    .chart__peak {
      position: absolute;
      top: 0;
      right: 0;
      display: flex;
      flex-direction: column;
      align-items: flex-end;
      padding: 0.4rem 0.7rem;
      border-radius: 0.75rem;
      background: var(--brand-primary-subtle);
    }
    .chart__peak-label {
      font-size: 0.62rem;
      font-weight: 700;
      letter-spacing: 0.08em;
      text-transform: uppercase;
      color: var(--brand-primary);
    }
    .chart__peak-value {
      font-size: 0.95rem;
      font-weight: 800;
      color: var(--brand-primary);
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

  readonly viewBox = `0 0 ${W} ${H}`;
  readonly padX = PAD_X;
  readonly innerRight = W - PAD_X;

  readonly maxValue = computed(() => {
    const values = this.chart().values;
    return values.length ? Math.max(...values, 1) : 1;
  });

  readonly peak = computed(() => {
    const values = this.chart().values;
    return values.length ? Math.max(...values) : 0;
  });

  readonly points = computed<ChartPoint[]>(() => {
    const { values, labels } = this.chart();
    const n = values.length;
    if (!n) return [];
    const max = this.maxValue();
    const innerW = W - PAD_X * 2;
    const innerH = H - PAD_TOP - PAD_BOTTOM;
    return values.map((value, i) => {
      const x = n === 1 ? W / 2 : PAD_X + (i / (n - 1)) * innerW;
      const y = PAD_TOP + innerH - (value / max) * innerH;
      return { x, y, value, label: labels[i] ?? '' };
    });
  });

  readonly gridLines = computed(() => {
    const innerH = H - PAD_TOP - PAD_BOTTOM;
    return [0, 0.5, 1].map((t) => PAD_TOP + innerH * t);
  });

  readonly linePath = computed(() => {
    const pts = this.points();
    if (!pts.length) return '';
    return pts.map((p, i) => `${i === 0 ? 'M' : 'L'} ${p.x} ${p.y}`).join(' ');
  });

  readonly areaPath = computed(() => {
    const pts = this.points();
    if (!pts.length) return '';
    const baseline = H - PAD_BOTTOM;
    const line = pts.map((p, i) => `${i === 0 ? 'M' : 'L'} ${p.x} ${p.y}`).join(' ');
    return `${line} L ${pts[pts.length - 1].x} ${baseline} L ${pts[0].x} ${baseline} Z`;
  });
}
