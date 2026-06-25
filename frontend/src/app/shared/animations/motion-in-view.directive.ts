import {
  Directive,
  ElementRef,
  afterNextRender,
  inject,
  input,
} from '@angular/core';
import { animate, inView } from 'motion';
import {
  MOTION_DEFAULTS,
  MotionPreset,
  getPreset,
  prefersReducedMotion,
} from './motion-presets';

/**
 * Animates an element into view when it scrolls into the viewport.
 *
 * Usage:
 *   <div motionInView></div>                       // default slide-up
 *   <div motionInView="scale" [motionDelay]="0.1"></div>
 *
 * Respects `prefers-reduced-motion`: when set, the element is shown
 * immediately in its final state with no animation.
 */
@Directive({
  selector: '[motionInView]',
})
export class MotionInViewDirective {
  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef);

  /** Entrance preset (empty string falls back to slide-up). */
  readonly motionInView = input<MotionPreset | ''>('');
  /** Delay before the animation starts, in seconds. */
  readonly motionDelay = input(0);
  /** Animation duration, in seconds. */
  readonly motionDuration = input(MOTION_DEFAULTS.duration as number);
  /** Animate only the first time it enters the viewport. */
  readonly motionOnce = input(true);
  /** Fraction of the element that must be visible to trigger (0-1). */
  readonly motionAmount = input(0.2);

  constructor() {
    afterNextRender(() => this.setup());
  }

  private setup(): void {
    const el = this.host.nativeElement;
    const preset = getPreset((this.motionInView() || 'slide-up') as MotionPreset);

    if (prefersReducedMotion()) {
      Object.assign(el.style, preset.to);
      return;
    }

    // Apply the hidden state up front so there is no flash of final content.
    animate(el, preset.from, { duration: 0 });

    inView(
      el,
      () => {
        animate(el, preset.to, {
          duration: this.motionDuration(),
          delay: this.motionDelay(),
          ease: MOTION_DEFAULTS.ease,
        });
        return this.motionOnce() ? undefined : () => animate(el, preset.from, { duration: 0.2 });
      },
      { amount: this.motionAmount() },
    );
  }
}
