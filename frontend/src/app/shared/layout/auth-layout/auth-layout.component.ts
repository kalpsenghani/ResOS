import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { ThemeService } from '../../../core/theme/theme.service';
import { MotionStaggerDirective } from '../../animations';

@Component({
  selector: 'app-auth-layout',
  imports: [RouterOutlet, MatIconModule, MotionStaggerDirective],
  templateUrl: './auth-layout.component.html',
  styleUrl: './auth-layout.component.scss',
})
export class AuthLayoutComponent {
  protected readonly highlights = [
    { icon: 'bolt', title: 'Run service in real time', copy: 'Live orders, kitchen and tables in one fast view.' },
    { icon: 'insights', title: 'Decisions backed by data', copy: 'Revenue, inventory and staff analytics built in.' },
    { icon: 'verified_user', title: 'Secure & multi-tenant', copy: 'Your data stays isolated and protected.' },
  ];

  constructor(protected readonly theme: ThemeService) {}
}
