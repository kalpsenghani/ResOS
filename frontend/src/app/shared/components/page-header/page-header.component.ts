import { Component, input } from '@angular/core';

@Component({
  selector: 'app-page-header',
  template: `
    <header class="page-header">
      <div>
        @if (breadcrumbs().length) {
          <nav class="breadcrumbs" aria-label="Breadcrumb">
            @for (crumb of breadcrumbs(); track crumb; let last = $last) {
              <span [class.active]="last">{{ crumb }}</span>
              @if (!last) {
                <span class="separator">/</span>
              }
            }
          </nav>
        }
        <h1>{{ title() }}</h1>
        @if (subtitle()) {
          <p>{{ subtitle() }}</p>
        }
      </div>
      <div class="actions">
        <ng-content select="[pageActions]" />
      </div>
    </header>
  `,
  styles: `
    .page-header {
      display: flex;
      align-items: flex-start;
      justify-content: space-between;
      gap: 1rem;
      margin-bottom: 1.5rem;
    }

    .breadcrumbs {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      margin-bottom: 0.5rem;
      font-size: 0.8125rem;
      color: var(--text-muted);
    }

    .breadcrumbs .active {
      color: var(--text-primary);
    }

    .separator {
      opacity: 0.5;
    }

    h1 {
      margin: 0;
      font-size: 1.75rem;
      font-weight: 700;
      letter-spacing: -0.02em;
    }

    p {
      margin: 0.375rem 0 0;
      color: var(--text-muted);
    }

    .actions:empty {
      display: none;
    }
  `,
})
export class PageHeaderComponent {
  readonly title = input.required<string>();
  readonly subtitle = input<string | undefined>(undefined);
  readonly breadcrumbs = input<string[]>([]);
}
