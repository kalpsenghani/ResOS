import { TestBed } from '@angular/core/testing';
import { ThemeService } from './theme.service';

describe('ThemeService', () => {
  beforeEach(() => {
    localStorage.clear();
    document.documentElement.removeAttribute('data-theme');
    document.body.classList.remove('dark-theme');
  });

  it('should persist theme selection to localStorage', () => {
    TestBed.configureTestingModule({});
    const service = TestBed.inject(ThemeService);

    service.setTheme('dark');
    expect(localStorage.getItem('resos-theme')).toBe('dark');
    expect(service.mode()).toBe('dark');

    service.setTheme('light');
    expect(localStorage.getItem('resos-theme')).toBe('light');
    expect(service.mode()).toBe('light');
  });

  it('should toggle between light and dark modes', () => {
    TestBed.configureTestingModule({});
    const service = TestBed.inject(ThemeService);

    service.setTheme('light');
    service.toggle();
    expect(service.mode()).toBe('dark');
    expect(localStorage.getItem('resos-theme')).toBe('dark');

    service.toggle();
    expect(service.mode()).toBe('light');
  });

  it('should apply theme to the document', () => {
    TestBed.configureTestingModule({});
    const service = TestBed.inject(ThemeService);

    service.setTheme('dark');
    TestBed.flushEffects();

    expect(document.documentElement.getAttribute('data-theme')).toBe('dark');
    expect(document.body.classList.contains('dark-theme')).toBeTrue();
  });
});
