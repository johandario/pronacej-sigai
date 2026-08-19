import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PlanTratamientoComponent } from './plan-tratamiento.component';

describe('PlanTratamientoComponent', () => {
  let component: PlanTratamientoComponent;
  let fixture: ComponentFixture<PlanTratamientoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PlanTratamientoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PlanTratamientoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
