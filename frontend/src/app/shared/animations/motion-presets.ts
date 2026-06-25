import type { DOMKeyframesDefinition, AnimationOptions } from 'motion';

/** Returns true when the user has asked the OS to minimize motion. */
export function prefersReducedMotion(): boolean {
  return (
    typeof window !== 'undefined' &&
    typeof window.matchMedia === 'function' &&
    window.matchMedia('(prefers-reduced-motion: reduce)').matches
  );
}

export type MotionPreset = 'fade' | 'slide-up' | 'slide-down' | 'slide-right' | 'scale' | 'blur';

interface PresetDefinition {
  /** Initial (hidden) state applied before the element animates in. */
  from: DOMKeyframesDefinition;
  /** Final (visible) state animated to. */
  to: DOMKeyframesDefinition;
}

/**
 * Entrance presets used by the motionInView + motionStagger directives.
 * Keep values subtle — this is a control-room UI, not a marketing splash.
 */
export const MOTION_PRESETS: Record<MotionPreset, PresetDefinition> = {
  fade: {
    from: { opacity: 0 },
    to: { opacity: 1 },
  },
  'slide-up': {
    from: { opacity: 0, transform: 'translateY(16px)' },
    to: { opacity: 1, transform: 'translateY(0px)' },
  },
  'slide-down': {
    from: { opacity: 0, transform: 'translateY(-16px)' },
    to: { opacity: 1, transform: 'translateY(0px)' },
  },
  'slide-right': {
    from: { opacity: 0, transform: 'translateX(-16px)' },
    to: { opacity: 1, transform: 'translateX(0px)' },
  },
  scale: {
    from: { opacity: 0, transform: 'scale(0.96)' },
    to: { opacity: 1, transform: 'scale(1)' },
  },
  blur: {
    from: { opacity: 0, filter: 'blur(8px)' },
    to: { opacity: 1, filter: 'blur(0px)' },
  },
};

/** Shared spring-like easing for a premium, smooth feel. */
export const MOTION_DEFAULTS: AnimationOptions = {
  duration: 0.5,
  ease: [0.22, 1, 0.36, 1],
};

export function getPreset(preset: MotionPreset): PresetDefinition {
  return MOTION_PRESETS[preset] ?? MOTION_PRESETS['slide-up'];
}
