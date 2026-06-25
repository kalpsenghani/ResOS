import { Component, input } from '@angular/core';

export type StatusVariant = 'success' | 'warning' | 'danger' | 'info' | 'neutral';

@Component({
  selector: 'app-status-badge',
  template: `<span class="badge" [class]="variantClass()">
    <span class="dot" [class.dot--pulse]="pulse()"></span>{{ label() }}
  </span>`,
  styles: `
    .badge {
      display: inline-flex;
      align-items: center;
      gap: 0.4rem;
      padding: 0.28rem 0.7rem;
      border-radius: 999px;
      font-size: 0.74rem;
      font-weight: 700;
      letter-spacing: 0.01em;
      text-transform: capitalize;
    }
    .dot {
      position: relative;
      width: 0.45rem;
      height: 0.45rem;
      border-radius: 999px;
      background: currentColor;
      flex: none;
    }
    .dot--pulse::after {
      content: '';
      position: absolute;
      inset: 0;
      border-radius: 999px;
      background: currentColor;
      animation: badge-pulse 1.8s cubic-bezier(0.4, 0, 0.2, 1) infinite;
    }
    @keyframes badge-pulse {
      0% { transform: scale(1); opacity: 0.55; }
      70% { transform: scale(2.6); opacity: 0; }
      100% { transform: scale(2.6); opacity: 0; }
    }
    @media (prefers-reduced-motion: reduce) {
      .dot--pulse::after { animation: none; }
    }
    .badge--success { background: var(--color-success-subtle); color: var(--color-success); }
    .badge--warning { background: var(--color-warning-subtle); color: var(--color-warning); }
    .badge--danger { background: var(--color-danger-subtle); color: var(--color-danger); }
    .badge--info { background: var(--color-info-subtle); color: var(--color-info); }
    .badge--neutral { background: var(--surface-hover); color: var(--text-muted); }
  `,
})
export class StatusBadgeComponent {
  readonly label = input.required<string>();
  readonly variant = input<StatusVariant>('neutral');
  readonly pulse = input(false);

  variantClass(): string {
    return `badge badge--${this.variant()}`;
  }
}
