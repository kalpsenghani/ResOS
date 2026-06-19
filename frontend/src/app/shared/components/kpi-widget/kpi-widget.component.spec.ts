import { TestBed } from '@angular/core/testing';
import { KpiWidgetComponent } from './kpi-widget.component';

describe('KpiWidgetComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [KpiWidgetComponent],
    }).compileComponents();
  });

  it('should render label and value', () => {
    const fixture = TestBed.createComponent(KpiWidgetComponent);
    fixture.componentRef.setInput('label', 'Revenue');
    fixture.componentRef.setInput('value', 4250);
    fixture.componentRef.setInput('change', 12.5);
    fixture.componentRef.setInput('trend', 'UP');
    fixture.componentRef.setInput('format', 'currency');
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Revenue');
    expect(compiled.textContent).toContain('+12.5%');
    expect(compiled.querySelector('.kpi__trend--up')).toBeTruthy();
  });

  it('should show down trend styling', () => {
    const fixture = TestBed.createComponent(KpiWidgetComponent);
    fixture.componentRef.setInput('label', 'Orders');
    fixture.componentRef.setInput('value', 87);
    fixture.componentRef.setInput('change', -3.2);
    fixture.componentRef.setInput('trend', 'DOWN');
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('.kpi__trend--down')).toBeTruthy();
    expect(compiled.textContent).toContain('-3.2%');
  });
});
