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
 * Staggers the entrance of a container's direct children when the container
 * scrolls into view. Ideal for card grids, KPI rows and table-like lists.
 *
 * Usage:
 *   <div motionStagger>
 *     <app-card></app-card>
 *     <app-card></app-card>
 *   </div>
 */
@Directive({
  selector: '[motionStagger]',
})
export class MotionStaggerDirective {
  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef);

  /** Entrance preset applied to each child. */
  readonly motionStagger = input<MotionPreset | ''>('');
  /** Delay between each child, in seconds. */
  readonly motionGap = input(0.06);
  /** Delay before the first child animates, in seconds. */
  readonly motionDelay = input(0);

  constructor() {
    afterNextRender(() => this.setup());
  }

  private setup(): void {
    const container = this.host.nativeElement;
    const children = Array.from(container.children) as HTMLElement[];
    if (!children.length) return;

    const preset = getPreset((this.motionStagger() || 'slide-up') as MotionPreset);

    if (prefersReducedMotion()) {
      children.forEach((child) => Object.assign(child.style, preset.to));
      return;
    }

    children.forEach((child) => animate(child, preset.from, { duration: 0 }));

    // Cap how many items are individually staggered so long lists (e.g. a
    // big orders table) still finish animating quickly.
    const maxStaggered = 14;

    inView(
      container,
      () => {
        children.forEach((child, index) => {
          animate(child, preset.to, {
            duration: MOTION_DEFAULTS.duration as number,
            delay: this.motionDelay() + Math.min(index, maxStaggered) * this.motionGap(),
            ease: MOTION_DEFAULTS.ease,
          });
        });
        return undefined;
      },
      { amount: 0.15 },
    );
  }
}
