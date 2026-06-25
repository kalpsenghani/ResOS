import { Component, input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-empty-state',
  imports: [MatIconModule],
  template: `
    <div class="empty-state">
      <span class="empty-state__art">
        <mat-icon>{{ icon() }}</mat-icon>
      </span>
      <h3>{{ title() }}</h3>
      @if (message()) {
        <p>{{ message() }}</p>
      }
      <div class="empty-state__cta">
        <ng-content />
      </div>
    </div>
  `,
  styles: `
    .empty-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      text-align: center;
      padding: 3.5rem 1.5rem;
      color: var(--text-muted);
      animation: es-in 0.45s ease both;
    }
    @keyframes es-in {
      from { opacity: 0; transform: scale(0.97); }
      to { opacity: 1; transform: scale(1); }
    }
    .empty-state__art {
      display: grid;
      place-items: center;
      width: 4.5rem;
      height: 4.5rem;
      border-radius: 1.4rem;
      margin-bottom: 1.25rem;
      background: var(--brand-primary-subtle);
      color: var(--brand-primary);
    }
    .empty-state__art mat-icon {
      font-size: 2.1rem;
      width: 2.1rem;
      height: 2.1rem;
    }
    h3 {
      margin: 0 0 0.5rem;
      color: var(--text-primary);
      font-size: 1.2rem;
      font-weight: 700;
    }
    p {
      margin: 0;
      max-width: 28rem;
      line-height: 1.55;
      font-size: 0.92rem;
    }
    .empty-state__cta:not(:empty) { margin-top: 1.5rem; }
  `,
})
export class EmptyStateComponent {
  readonly title = input('No data yet');
  readonly message = input<string | undefined>(undefined);
  readonly icon = input('inbox');
}
