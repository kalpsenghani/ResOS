import { Component, input, output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

export type ButtonVariant = 'primary' | 'secondary' | 'ghost' | 'danger';

@Component({
  selector: 'app-button',
  imports: [MatButtonModule, MatIconModule],
  template: `
    @if (variant() === 'primary') {
      <button
        mat-flat-button
        [type]="type()"
        [disabled]="disabled()"
        [class.full-width]="fullWidth()"
        (click)="clicked.emit($event)"
      >
        @if (icon()) {
          <mat-icon>{{ icon() }}</mat-icon>
        }
        <ng-content />
      </button>
    } @else if (variant() === 'danger') {
      <button
        mat-flat-button
        color="warn"
        [type]="type()"
        [disabled]="disabled()"
        [class.full-width]="fullWidth()"
        (click)="clicked.emit($event)"
      >
        @if (icon()) {
          <mat-icon>{{ icon() }}</mat-icon>
        }
        <ng-content />
      </button>
    } @else if (variant() === 'secondary') {
      <button
        mat-stroked-button
        [type]="type()"
        [disabled]="disabled()"
        [class.full-width]="fullWidth()"
        (click)="clicked.emit($event)"
      >
        @if (icon()) {
          <mat-icon>{{ icon() }}</mat-icon>
        }
        <ng-content />
      </button>
    } @else {
      <button
        mat-button
        [type]="type()"
        [disabled]="disabled()"
        [class.full-width]="fullWidth()"
        (click)="clicked.emit($event)"
      >
        @if (icon()) {
          <mat-icon>{{ icon() }}</mat-icon>
        }
        <ng-content />
      </button>
    }
  `,
  styles: `
    :host {
      display: inline-block;
    }

    button.full-width {
      width: 100%;
    }

    mat-icon {
      margin-right: 0.375rem;
      font-size: 1.125rem;
      width: 1.125rem;
      height: 1.125rem;
    }
  `,
})
export class ButtonComponent {
  readonly variant = input<ButtonVariant>('primary');
  readonly type = input<'button' | 'submit' | 'reset'>('button');
  readonly disabled = input(false);
  readonly fullWidth = input(false);
  readonly icon = input<string | undefined>(undefined);
  readonly clicked = output<MouseEvent>();
}
