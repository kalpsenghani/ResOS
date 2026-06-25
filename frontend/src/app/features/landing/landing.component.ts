import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { ThemeService } from '../../core/theme/theme.service';
import { MotionInViewDirective, MotionStaggerDirective } from '../../shared/animations';

@Component({
  selector: 'app-landing',
  imports: [RouterLink, MatIconModule, MotionStaggerDirective, MotionInViewDirective],
  templateUrl: './landing.component.html',
  styleUrl: './landing.component.scss',
})
export class LandingComponent {
  protected readonly theme = inject(ThemeService);

  protected readonly stats = [
    { value: '40%', label: 'Faster service cycles' },
    { value: '1', label: 'Unified control panel' },
    { value: '24/7', label: 'Live operational visibility' },
  ];

  protected readonly features = [
    {
      icon: 'receipt_long',
      title: 'Orders & kitchen',
      copy: 'Route dine-in, takeout, and delivery from one live queue with kitchen-ready workflows.',
    },
    {
      icon: 'event_seat',
      title: 'Reservations',
      copy: 'Manage tables, bookings, and guest flow without spreadsheets or phone-tag chaos.',
    },
    {
      icon: 'inventory_2',
      title: 'Inventory',
      copy: 'Track stock, suppliers, and low-stock alerts before they become service problems.',
    },
    {
      icon: 'groups',
      title: 'Staff & scheduling',
      copy: 'Keep roles, shifts, and team availability aligned with daily service demand.',
    },
    {
      icon: 'insights',
      title: 'Analytics',
      copy: 'See revenue, labor, inventory, and order trends in dashboards built for operators.',
    },
    {
      icon: 'verified_user',
      title: 'Multi-tenant security',
      copy: 'Every restaurant workspace stays isolated with secure sign-in and tenant boundaries.',
    },
  ];

  protected readonly steps = [
    {
      title: 'Create your workspace',
      copy: 'Register your restaurant, invite your team, and configure locations in minutes.',
    },
    {
      title: 'Run daily operations',
      copy: 'Take orders, seat guests, manage inventory, and coordinate the kitchen from one place.',
    },
    {
      title: 'Improve with data',
      copy: 'Use built-in analytics to spot bottlenecks, protect margins, and grow confidently.',
    },
  ];
}
