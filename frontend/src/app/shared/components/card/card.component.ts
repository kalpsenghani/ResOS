import { Component, input } from '@angular/core';

@Component({
  selector: 'app-card',
  template: `
    <section class="card" [class.card--flat]="flat()">
      @if (title() || subtitle()) {
        <header class="card__header">
          @if (title()) {
            <h3 class="card__title">{{ title() }}</h3>
          }
          @if (subtitle()) {
            <p class="card__subtitle">{{ subtitle() }}</p>
          }
          <div class="card__actions">
            <ng-content select="[cardActions]" />
          </div>
        </header>
      }
      <div class="card__body">
        <ng-content />
      </div>
      <footer class="card__footer">
        <ng-content select="[cardFooter]" />
      </footer>
    </section>
  `,
  styles: `
    .card {
      background: var(--surface-card);
      border: 1px solid var(--border-subtle);
      border-radius: 0.75rem;
      box-shadow: var(--shadow-sm);
      overflow: hidden;
    }

    .card--flat {
      box-shadow: none;
    }

    .card__header {
      display: flex;
      align-items: flex-start;
      justify-content: space-between;
      gap: 1rem;
      padding: 1.25rem 1.25rem 0;
    }

    .card__title {
      margin: 0;
      font-size: 1rem;
      font-weight: 600;
      color: var(--text-primary);
    }

    .card__subtitle {
      margin: 0.25rem 0 0;
      font-size: 0.875rem;
      color: var(--text-muted);
    }

    .card__actions:empty {
      display: none;
    }

    .card__body {
      padding: 1.25rem;
    }

    .card__header + .card__body {
      padding-top: 1rem;
    }

    .card__footer:not(:empty) {
      padding: 0 1.25rem 1.25rem;
      border-top: 1px solid var(--border-subtle);
    }
  `,
})
export class CardComponent {
  readonly title = input<string | undefined>(undefined);
  readonly subtitle = input<string | undefined>(undefined);
  readonly flat = input(false);
}
