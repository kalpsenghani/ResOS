import { Component, input } from '@angular/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-loading-spinner',
  imports: [MatProgressSpinnerModule],
  template: `
    <div class="spinner" [class.spinner--overlay]="overlay()">
      <mat-spinner [diameter]="size()" />
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
      padding: 2rem;
      color: var(--text-muted);
    }

    .spinner--overlay {
      position: absolute;
      inset: 0;
      background: color-mix(in srgb, var(--surface-bg) 80%, transparent);
      z-index: 10;
    }

    p {
      margin: 0;
      font-size: 0.875rem;
    }
  `,
})
export class LoadingSpinnerComponent {
  readonly message = input<string | undefined>(undefined);
  readonly size = input(40);
  readonly overlay = input(false);
}
