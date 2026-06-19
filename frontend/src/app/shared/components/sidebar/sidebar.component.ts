import { Component, input, output } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';

export interface SidebarNavItem {
  label: string;
  icon: string;
  route: string;
}

@Component({
  selector: 'app-sidebar',
  imports: [RouterLink, RouterLinkActive, MatIconModule, MatTooltipModule],
  template: `
    <aside class="sidebar" [class.sidebar--collapsed]="collapsed()">
      <div class="sidebar__brand">
        <span class="brand-mark">R</span>
        @if (!collapsed()) {
          <span class="brand-name">ResOS</span>
        }
      </div>

      <nav class="sidebar__nav">
        @for (item of items(); track item.route) {
          <a
            [routerLink]="item.route"
            routerLinkActive="active"
            [matTooltip]="collapsed() ? item.label : ''"
            matTooltipPosition="right"
          >
            <mat-icon>{{ item.icon }}</mat-icon>
            @if (!collapsed()) {
              <span>{{ item.label }}</span>
            }
          </a>
        }
      </nav>

      <button type="button" class="sidebar__toggle" (click)="toggleCollapsed.emit()">
        <mat-icon>{{ collapsed() ? 'chevron_right' : 'chevron_left' }}</mat-icon>
      </button>
    </aside>
  `,
  styles: `
    .sidebar {
      display: flex;
      flex-direction: column;
      width: 16rem;
      min-height: 100%;
      background: var(--surface-sidebar);
      border-right: 1px solid var(--border-subtle);
      transition: width 0.2s ease;
    }

    .sidebar--collapsed {
      width: 4.5rem;
    }

    .sidebar__brand {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      padding: 1.25rem 1rem;
      border-bottom: 1px solid var(--border-subtle);
    }

    .brand-mark {
      width: 2.25rem;
      height: 2.25rem;
      border-radius: 0.625rem;
      display: grid;
      place-items: center;
      background: var(--brand-primary);
      color: white;
      font-weight: 700;
      flex-shrink: 0;
    }

    .brand-name {
      font-size: 1.125rem;
      font-weight: 600;
      letter-spacing: -0.02em;
      white-space: nowrap;
    }

    .sidebar__nav {
      display: flex;
      flex-direction: column;
      gap: 0.25rem;
      padding: 1rem 0.75rem;
      flex: 1;
    }

    .sidebar__nav a {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      padding: 0.75rem;
      border-radius: 0.5rem;
      color: var(--text-muted);
      text-decoration: none;
      font-size: 0.9375rem;
      font-weight: 500;
      transition: background 0.15s ease, color 0.15s ease;
    }

    .sidebar__nav a:hover,
    .sidebar__nav a.active {
      background: var(--brand-primary-subtle);
      color: var(--brand-primary);
    }

    .sidebar__nav mat-icon {
      flex-shrink: 0;
    }

    .sidebar__toggle {
      margin: 0.75rem;
      border: 1px solid var(--border-subtle);
      background: transparent;
      color: var(--text-muted);
      border-radius: 0.5rem;
      padding: 0.5rem;
      cursor: pointer;
    }
  `,
})
export class SidebarComponent {
  readonly items = input.required<SidebarNavItem[]>();
  readonly collapsed = input(false);
  readonly toggleCollapsed = output<void>();
}
