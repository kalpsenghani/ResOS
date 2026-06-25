import { animate, query, style, transition, trigger } from '@angular/animations';

/**
 * Smooth fade + lift for entering routed views. Only the entering view is
 * animated (the leaving view is removed instantly) so there is never any
 * visual overlap between two routes.
 *
 * Attach to the element wrapping <router-outlet> with
 * [@routeFade]="getRouteState(outlet)".
 */
export const routeFade = trigger('routeFade', [
  transition('* => *', [
    query(
      ':enter',
      [
        style({ opacity: 0, transform: 'translateY(10px)' }),
        animate(
          '320ms cubic-bezier(0.22, 1, 0.36, 1)',
          style({ opacity: 1, transform: 'translateY(0)' }),
        ),
      ],
      { optional: true },
    ),
  ]),
]);
