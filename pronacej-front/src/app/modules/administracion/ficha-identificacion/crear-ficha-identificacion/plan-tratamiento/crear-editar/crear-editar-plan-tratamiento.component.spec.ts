import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CrearEditarPlanTratamientoComponent } from './crear-editar-plan-tratamiento.component';

describe('CrearEditarPlanTratamientoComponent', () => {
  let component: CrearEditarPlanTratamientoComponent;
  let fixture: ComponentFixture<CrearEditarPlanTratamientoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CrearEditarPlanTratamientoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CrearEditarPlanTratamientoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
