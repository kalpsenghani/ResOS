import {
  ChangeDetectionStrategy,
  Component,
  computed,
  forwardRef,
  input,
  signal,
  viewChild,
} from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import {
  CdkOverlayOrigin,
  ConnectedPosition,
  OverlayModule,
} from '@angular/cdk/overlay';

export interface SelectOption {
  value: unknown;
  label: string;
}

@Component({
  selector: 'app-select',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [OverlayModule],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => SelectComponent),
      multi: true,
    },
  ],
  template: `
    <button
      type="button"
      class="input select-trigger"
      [class.is-open]="open()"
      [disabled]="disabled()"
      (click)="toggle()"
      (blur)="onTouched()"
      cdkOverlayOrigin
      #origin="cdkOverlayOrigin"
    >
      <span class="select-value" [class.is-placeholder]="selectedLabel() === null">
        {{ selectedLabel() ?? placeholder() }}
      </span>
      <svg
        class="select-arrow"
        viewBox="0 0 24 24"
        width="16"
        height="16"
        fill="none"
        stroke="currentColor"
        stroke-width="2.5"
        stroke-linecap="round"
        stroke-linejoin="round"
        aria-hidden="true"
      >
        <polyline points="6 9 12 15 18 9" />
      </svg>
    </button>

    <ng-template
      cdkConnectedOverlay
      [cdkConnectedOverlayOrigin]="origin"
      [cdkConnectedOverlayOpen]="open()"
      [cdkConnectedOverlayWidth]="panelWidth()"
      [cdkConnectedOverlayViewportMargin]="8"
      [cdkConnectedOverlayPositions]="positions"
      (overlayOutsideClick)="close()"
      (detach)="close()"
    >
      <div class="select-panel" role="listbox">
        @for (opt of options(); track opt.value) {
          <button
            type="button"
            role="option"
            class="select-option"
            [class.is-selected]="opt.value === value()"
            [attr.aria-selected]="opt.value === value()"
            (click)="choose(opt)"
          >
            <span>{{ opt.label }}</span>
            @if (opt.value === value()) {
              <svg
                viewBox="0 0 24 24"
                width="16"
                height="16"
                fill="none"
                stroke="currentColor"
                stroke-width="3"
                stroke-linecap="round"
                stroke-linejoin="round"
                aria-hidden="true"
              >
                <polyline points="20 6 9 17 4 12" />
              </svg>
            }
          </button>
        }
      </div>
    </ng-template>
  `,
})
export class SelectComponent implements ControlValueAccessor {
  readonly options = input<SelectOption[]>([]);
  readonly placeholder = input('Select…');

  protected readonly value = signal<unknown>(null);
  protected readonly open = signal(false);
  protected readonly disabled = signal(false);
  protected readonly panelWidth = signal(0);

  private readonly origin = viewChild.required<CdkOverlayOrigin>('origin');

  protected readonly positions: ConnectedPosition[] = [
    { originX: 'start', originY: 'bottom', overlayX: 'start', overlayY: 'top', offsetY: 6 },
    { originX: 'start', originY: 'top', overlayX: 'start', overlayY: 'bottom', offsetY: -6 },
  ];

  protected readonly selectedLabel = computed(() => {
    const current = this.value();
    const match = this.options().find((opt) => opt.value === current);
    return match ? match.label : null;
  });

  onChange: (value: unknown) => void = () => {};
  onTouched: () => void = () => {};

  protected toggle(): void {
    if (this.disabled()) {
      return;
    }
    if (!this.open()) {
      this.panelWidth.set(this.origin().elementRef.nativeElement.offsetWidth);
    }
    this.open.update((isOpen) => !isOpen);
  }

  protected close(): void {
    if (this.open()) {
      this.open.set(false);
      this.onTouched();
    }
  }

  protected choose(opt: SelectOption): void {
    this.value.set(opt.value);
    this.onChange(opt.value);
    this.close();
  }

  writeValue(value: unknown): void {
    this.value.set(value);
  }

  registerOnChange(fn: (value: unknown) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled.set(isDisabled);
  }
}
