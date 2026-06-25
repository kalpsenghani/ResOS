import { Component, input } from '@angular/core';

@Component({
  selector: 'app-card',
  template: `
    <section class="card" [class.card-interactive]="interactive()" [class.card--flat]="flat()">
      @if (title() || subtitle()) {
        <header class="card__header">
          <div class="card__heading">
            @if (eyebrow()) {
              <span class="card__eyebrow">{{ eyebrow() }}</span>
            }
            @if (title()) {
              <h3 class="card__title">{{ title() }}</h3>
            }
            @if (subtitle()) {
              <p class="card__subtitle">{{ subtitle() }}</p>
            }
          </div>
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
      border-radius: 1.25rem;
      box-shadow: var(--shadow-sm);
      overflow: hidden;
      transition: box-shadow 0.25s ease, transform 0.25s ease, border-color 0.25s ease;
    }
    .card-interactive:hover {
      box-shadow: var(--shadow-md);
      transform: translateY(-2px);
      border-color: var(--border-strong);
    }
    .card--flat {
      box-shadow: none;
    }
    .card__header {
      display: flex;
      align-items: flex-start;
      justify-content: space-between;
      gap: 1rem;
      padding: 1.35rem 1.35rem 0;
    }
    .card__eyebrow {
      display: block;
      font-size: 0.7rem;
      font-weight: 700;
      letter-spacing: 0.08em;
      text-transform: uppercase;
      color: var(--brand-primary);
      margin-bottom: 0.35rem;
    }
    .card__title {
      margin: 0;
      font-size: 1.05rem;
      font-weight: 700;
      letter-spacing: -0.02em;
      color: var(--text-primary);
    }
    .card__subtitle {
      margin: 0.2rem 0 0;
      font-size: 0.85rem;
      color: var(--text-muted);
    }
    .card__actions:empty {
      display: none;
    }
    .card__body {
      padding: 1.35rem;
    }
    .card__header + .card__body {
      padding-top: 1rem;
    }
    .card__footer:not(:empty) {
      padding: 0 1.35rem 1.35rem;
      border-top: 1px solid var(--border-subtle);
    }
  `,
})
export class CardComponent {
  readonly title = input<string | undefined>(undefined);
  readonly subtitle = input<string | undefined>(undefined);
  readonly eyebrow = input<string | undefined>(undefined);
  readonly flat = input(false);
  readonly interactive = input(false);
}
