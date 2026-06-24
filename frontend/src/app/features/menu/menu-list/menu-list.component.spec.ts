import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { MenuListComponent } from './menu-list.component';

describe('MenuListComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MenuListComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
  });

  it('should create', () => {
    const fixture = TestBed.createComponent(MenuListComponent);
    expect(fixture.componentInstance).toBeTruthy();
  });
});
