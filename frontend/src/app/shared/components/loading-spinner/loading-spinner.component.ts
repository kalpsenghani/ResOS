import { Component, input } from '@angular/core';

@Component({
  selector: 'app-loading-spinner',
  template: `
    <div class="spinner" [class.spinner--overlay]="overlay()">
      <span class="loader" [style.width.px]="size()" [style.height.px]="size()"></span>
      @if (message()) {
        <p>{{ message() }}</p>
      }
    </div>
  `,
  styles: `
    .spinner {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 1rem;
      padding: 2.5rem;
      color: var(--text-muted);
      animation: spinner-in 0.35s ease both;
    }
    @keyframes spinner-in {
      from { opacity: 0; transform: translateY(4px); }
      to { opacity: 1; transform: translateY(0); }
    }
    @media (prefers-reduced-motion: reduce) {
      .spinner { animation: none; }
    }
    .spinner--overlay {
      position: absolute;
      inset: 0;
      background: color-mix(in srgb, var(--surface-bg) 75%, transparent);
      backdrop-filter: blur(2px);
      z-index: 10;
    }
    .loader {
      display: inline-block;
      border-radius: 999px;
      border: 3px solid var(--brand-primary-subtle);
      border-top-color: var(--brand-primary);
      animation: loader-spin 0.7s cubic-bezier(0.4, 0, 0.2, 1) infinite;
    }
    @keyframes loader-spin {
      to { transform: rotate(360deg); }
    }
    p {
      margin: 0;
      font-size: 0.88rem;
      font-weight: 500;
    }
  `,
})
export class LoadingSpinnerComponent {
  readonly message = input<string | undefined>(undefined);
  readonly size = input(40);
  readonly overlay = input(false);
}
