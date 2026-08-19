import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SeguimientoPtiComponent } from './seguimiento-pti.component';

describe('SeguimientoPtiComponent', () => {
  let component: SeguimientoPtiComponent;
  let fixture: ComponentFixture<SeguimientoPtiComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SeguimientoPtiComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SeguimientoPtiComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
