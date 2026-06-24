import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { EmployeeListComponent } from './employee-list.component';

describe('EmployeeListComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EmployeeListComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
  });

  it('should create', () => {
    const fixture = TestBed.createComponent(EmployeeListComponent);
    expect(fixture.componentInstance).toBeTruthy();
  });
});
