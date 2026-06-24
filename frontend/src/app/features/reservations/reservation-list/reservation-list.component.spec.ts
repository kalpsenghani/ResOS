import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ReservationListComponent } from './reservation-list.component';

describe('ReservationListComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReservationListComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
  });

  it('should create', () => {
    const fixture = TestBed.createComponent(ReservationListComponent);
    expect(fixture.componentInstance).toBeTruthy();
  });
});
