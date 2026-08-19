import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RegistroActividadPlanComponent } from './registro-actividad-plan.component';

describe('RegistroActividadPlanComponent', () => {
  let component: RegistroActividadPlanComponent;
  let fixture: ComponentFixture<RegistroActividadPlanComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RegistroActividadPlanComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RegistroActividadPlanComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
