import { Component, computed, input, output } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

export type ButtonVariant = 'primary' | 'secondary' | 'ghost' | 'danger' | 'dark';
export type ButtonSize = 'sm' | 'md' | 'lg';

@Component({
  selector: 'app-button',
  imports: [MatIconModule],
  host: {
    class: 'app-button-host',
    '[class.app-button-host--full]': 'fullWidth()',
  },
  template: `
    <button
      [type]="type()"
      [disabled]="disabled() || loading()"
      [class]="classes()"
      (click)="clicked.emit($event)"
    >
      @if (loading()) {
        <span class="btn-spinner" aria-hidden="true"></span>
      } @else if (icon()) {
        <mat-icon class="btn-glyph">{{ icon() }}</mat-icon>
      }
      <ng-content />
    </button>
  `,
  styles: `
    :host.app-button-host {
      display: inline-flex;
      vertical-align: middle;
      max-width: 100%;
    }
    :host.app-button-host--full {
      display: flex;
      width: 100%;
    }
    button {
      width: auto;
    }
    :host.app-button-host--full button {
      width: 100%;
    }
    .btn-glyph {
      font-size: 1.15rem;
      width: 1.15rem;
      height: 1.15rem;
    }
    .btn-spinner {
      width: 1.05rem;
      height: 1.05rem;
      border-radius: 999px;
      border: 2px solid currentColor;
      border-right-color: transparent;
      animation: btn-spin 0.6s linear infinite;
    }
    @keyframes btn-spin {
      to { transform: rotate(360deg); }
    }
  `,
})
export class ButtonComponent {
  readonly variant = input<ButtonVariant>('primary');
  readonly size = input<ButtonSize>('md');
  readonly type = input<'button' | 'submit' | 'reset'>('button');
  readonly disabled = input(false);
  readonly loading = input(false);
  readonly fullWidth = input(false);
  readonly icon = input<string | undefined>(undefined);
  readonly clicked = output<MouseEvent>();

  readonly classes = computed(() => {
    const map: Record<ButtonVariant, string> = {
      primary: 'btn-primary',
      secondary: 'btn-secondary',
      ghost: 'btn-ghost',
      danger: 'btn-danger',
      dark: 'btn-dark',
    };
    const sizes: Record<ButtonSize, string> = { sm: 'btn-sm', md: '', lg: 'btn-lg' };
    return ['btn', map[this.variant()], sizes[this.size()], this.fullWidth() ? 'w-full' : '']
      .filter(Boolean)
      .join(' ');
  });
}
