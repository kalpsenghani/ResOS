import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { AnalyticsDashboardComponent } from './analytics-dashboard.component';

describe('AnalyticsDashboardComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AnalyticsDashboardComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
  });

  it('should create', () => {
    const fixture = TestBed.createComponent(AnalyticsDashboardComponent);
    expect(fixture.componentInstance).toBeTruthy();
  });
});
