import { Component, input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-empty-state',
  imports: [MatIconModule],
  template: `
    <div class="empty-state">
      <mat-icon>{{ icon() }}</mat-icon>
      <h3>{{ title() }}</h3>
      @if (message()) {
        <p>{{ message() }}</p>
      }
      <ng-content />
    </div>
  `,
  styles: `
    .empty-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      text-align: center;
      padding: 3rem 1.5rem;
      color: var(--text-muted);
    }

    mat-icon {
      font-size: 3rem;
      width: 3rem;
      height: 3rem;
      margin-bottom: 1rem;
      opacity: 0.6;
    }

    h3 {
      margin: 0 0 0.5rem;
      color: var(--text-primary);
      font-size: 1.125rem;
    }

    p {
      margin: 0;
      max-width: 28rem;
      line-height: 1.5;
    }
  `,
})
export class EmptyStateComponent {
  readonly title = input('No data yet');
  readonly message = input<string | undefined>(undefined);
  readonly icon = input('inbox');
}
