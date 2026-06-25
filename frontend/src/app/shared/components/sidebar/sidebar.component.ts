import { Component, input, output } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MotionStaggerDirective } from '../../animations';

export interface SidebarNavItem {
  label: string;
  icon: string;
  route: string;
}

export interface SidebarNavGroup {
  label: string;
  items: SidebarNavItem[];
}

@Component({
  selector: 'app-sidebar',
  imports: [RouterLink, RouterLinkActive, MatIconModule, MatTooltipModule, MotionStaggerDirective],
  template: `
    <aside class="sidebar" [class.sidebar--collapsed]="collapsed()">
      <div class="sidebar__brand">
        <span class="brand-mark">R</span>
        @if (!collapsed()) {
          <span class="brand-text">
            <span class="brand-name">ResOS</span>
            <span class="brand-tag">Restaurant OS</span>
          </span>
        }
      </div>

      <nav class="sidebar__nav" motionStagger="slide-right" [motionGap]="0.05">
        @for (group of groups(); track group.label) {
          <div class="nav-group">
            @if (!collapsed()) {
              <p class="nav-group__label">{{ group.label }}</p>
            }
            @for (item of group.items; track item.route) {
              <a
                class="nav-link"
                [routerLink]="item.route"
                routerLinkActive="nav-link--active"
                [matTooltip]="collapsed() ? item.label : ''"
                matTooltipPosition="right"
              >
                <span class="nav-link__bar"></span>
                <mat-icon class="nav-link__icon">{{ item.icon }}</mat-icon>
                @if (!collapsed()) {
                  <span class="nav-link__label">{{ item.label }}</span>
                }
              </a>
            }
          </div>
        }
      </nav>

      <button type="button" class="sidebar__toggle" (click)="toggleCollapsed.emit()">
        <mat-icon>{{ collapsed() ? 'chevron_right' : 'chevron_left' }}</mat-icon>
        @if (!collapsed()) {
          <span>Collapse</span>
        }
      </button>
    </aside>
  `,
  styles: `
    .sidebar {
      display: flex;
      flex-direction: column;
      width: 16rem;
      height: 100%;
      background: var(--surface-sidebar);
      border-right: 1px solid var(--sidebar-border);
      transition: width 0.28s cubic-bezier(0.4, 0, 0.2, 1);
      overflow: hidden;
    }
    .sidebar--collapsed { width: 4.75rem; }

    .sidebar__brand {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      padding: 1.35rem 1.25rem;
      margin-bottom: 0.5rem;
    }
    .brand-mark {
      width: 2.4rem;
      height: 2.4rem;
      border-radius: 0.8rem;
      display: grid;
      place-items: center;
      background: linear-gradient(135deg, var(--brand-primary), var(--accent));
      color: #fff;
      font-weight: 800;
      font-size: 1.15rem;
      flex-shrink: 0;
      box-shadow: var(--shadow-glow);
    }
    .brand-text { display: flex; flex-direction: column; line-height: 1.1; }
    .brand-name { font-weight: 800; font-size: 1.05rem; color: #fff; letter-spacing: -0.02em; }
    .brand-tag { font-size: 0.68rem; color: var(--sidebar-text); font-weight: 500; }

    .sidebar__nav {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 1.1rem;
      padding: 0 0.75rem;
      overflow-y: auto;
    }
    .nav-group { display: flex; flex-direction: column; gap: 0.15rem; }
    .nav-group__label {
      margin: 0 0 0.35rem 0.85rem;
      font-size: 0.66rem;
      font-weight: 700;
      letter-spacing: 0.1em;
      text-transform: uppercase;
      color: var(--sidebar-text);
      opacity: 0.7;
    }

    .nav-link {
      position: relative;
      display: flex;
      align-items: center;
      gap: 0.85rem;
      padding: 0.65rem 0.85rem;
      border-radius: 0.85rem;
      color: var(--sidebar-text);
      font-weight: 600;
      font-size: 0.9rem;
      transition: color 0.2s ease, background-color 0.2s ease;
    }
    .nav-link:hover { background: var(--sidebar-hover-bg); color: var(--sidebar-text-active); }
    .nav-link__bar {
      position: absolute;
      left: 0;
      top: 50%;
      transform: translateY(-50%) scaleY(0);
      width: 3px;
      height: 1.5rem;
      border-radius: 999px;
      background: var(--brand-primary);
      transition: transform 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);
    }
    .nav-link__icon { font-size: 1.35rem; width: 1.35rem; height: 1.35rem; flex-shrink: 0; }
    .nav-link__label { white-space: nowrap; }

    .nav-link--active {
      background: var(--sidebar-active-bg);
      color: var(--sidebar-text-active);
    }
    .nav-link--active .nav-link__bar { transform: translateY(-50%) scaleY(1); }
    .nav-link--active .nav-link__icon { color: var(--brand-primary); }

    .sidebar--collapsed .nav-link { justify-content: center; padding: 0.65rem; }

    .sidebar__toggle {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      margin: 0.75rem;
      padding: 0.6rem 0.85rem;
      border: 0;
      border-radius: 0.85rem;
      background: transparent;
      color: var(--sidebar-text);
      font-weight: 600;
      font-size: 0.85rem;
      cursor: pointer;
      transition: background-color 0.2s ease, color 0.2s ease;
    }
    .sidebar__toggle:hover { background: var(--sidebar-hover-bg); color: var(--sidebar-text-active); }
    .sidebar--collapsed .sidebar__toggle { justify-content: center; }
    .sidebar__toggle mat-icon { font-size: 1.25rem; width: 1.25rem; height: 1.25rem; }
  `,
})
export class SidebarComponent {
  readonly groups = input<SidebarNavGroup[]>([]);
  readonly collapsed = input(false);
  readonly toggleCollapsed = output<void>();
}
