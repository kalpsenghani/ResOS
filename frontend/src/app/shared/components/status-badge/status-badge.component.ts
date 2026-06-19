import { Component, input } from '@angular/core';

export type StatusVariant = 'success' | 'warning' | 'danger' | 'info' | 'neutral';

@Component({
  selector: 'app-status-badge',
  template: `<span class="badge" [class]="variantClass()">{{ label() }}</span>`,
  styles: `
    .badge {
      display: inline-flex;
      align-items: center;
      padding: 0.125rem 0.625rem;
      border-radius: 999px;
      font-size: 0.75rem;
      font-weight: 600;
      text-transform: capitalize;
    }

    .badge--success {
      background: var(--color-success-subtle);
      color: var(--color-success);
    }

    .badge--warning {
      background: var(--color-warning-subtle);
      color: var(--color-warning);
    }

    .badge--danger {
      background: var(--color-danger-subtle);
      color: var(--color-danger);
    }

    .badge--info {
      background: var(--color-info-subtle);
      color: var(--color-info);
    }

    .badge--neutral {
      background: var(--border-subtle);
      color: var(--text-muted);
    }
  `,
})
export class StatusBadgeComponent {
  readonly label = input.required<string>();
  readonly variant = input<StatusVariant>('neutral');

  variantClass(): string {
    return `badge badge--${this.variant()}`;
  }
}
