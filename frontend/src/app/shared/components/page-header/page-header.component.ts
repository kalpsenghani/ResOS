import { Component, input } from '@angular/core';

@Component({
  selector: 'app-page-header',
  template: `
    <header class="page-header">
      <div class="page-header__lead">
        @if (breadcrumbs().length) {
          <nav class="breadcrumbs" aria-label="Breadcrumb">
            @for (crumb of breadcrumbs(); track crumb; let last = $last) {
              <span [class.active]="last">{{ crumb }}</span>
              @if (!last) {
                <span class="separator">›</span>
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
      align-items: flex-end;
      justify-content: space-between;
      gap: 1rem;
      margin-bottom: 1.75rem;
      animation: ph-in 0.45s cubic-bezier(0.4, 0, 0.2, 1) both;
    }
    @keyframes ph-in {
      from { opacity: 0; transform: translateY(-8px); }
      to { opacity: 1; transform: translateY(0); }
    }
    .breadcrumbs {
      display: flex;
      align-items: center;
      gap: 0.45rem;
      margin-bottom: 0.55rem;
      font-size: 0.78rem;
      font-weight: 600;
      color: var(--text-muted);
    }
    .breadcrumbs .active { color: var(--brand-primary); }
    .separator { opacity: 0.5; }
    h1 {
      margin: 0;
      font-size: 1.9rem;
      font-weight: 800;
      letter-spacing: -0.035em;
    }
    p {
      margin: 0.4rem 0 0;
      color: var(--text-muted);
      font-size: 0.95rem;
    }
    .actions {
      display: flex;
      align-items: center;
      gap: 0.6rem;
    }
    .actions:empty { display: none; }

    @media (max-width: 640px) {
      .page-header { flex-direction: column; align-items: flex-start; }
      h1 { font-size: 1.5rem; }
    }
  `,
})
export class PageHeaderComponent {
  readonly title = input.required<string>();
  readonly subtitle = input<string | undefined>(undefined);
  readonly breadcrumbs = input<string[]>([]);
}
