import { Component, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { map } from 'rxjs';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';
import { CardComponent } from '../../shared/components/card/card.component';

@Component({
  selector: 'app-feature-placeholder',
  imports: [PageHeaderComponent, EmptyStateComponent, CardComponent],
  template: `
    <app-page-header [title]="title() ?? 'Module'" [subtitle]="subtitle()" />

    <app-card>
      <app-empty-state
        [icon]="icon() ?? 'construction'"
        [title]="title() ?? 'Coming soon'"
        [message]="message()"
      />
    </app-card>
  `,
})
export class FeaturePlaceholderComponent {
  private readonly route = inject(ActivatedRoute);

  readonly title = toSignal(this.route.data.pipe(map((data) => data['title'] as string | undefined)));
  readonly subtitle = toSignal(
    this.route.data.pipe(map((data) => data['subtitle'] as string | undefined)),
  );
  readonly message = toSignal(
    this.route.data.pipe(
      map(
        (data) =>
          (data['message'] as string | undefined) ??
          'This module will be available in a future release.',
      ),
    ),
  );
  readonly icon = toSignal(this.route.data.pipe(map((data) => data['icon'] as string | undefined)));
}
