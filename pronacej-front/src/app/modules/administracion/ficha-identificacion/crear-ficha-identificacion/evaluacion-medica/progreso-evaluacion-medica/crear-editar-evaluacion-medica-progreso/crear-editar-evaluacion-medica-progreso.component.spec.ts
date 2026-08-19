import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CrearEditarEvaluacionMedicaProgresoComponent } from './crear-editar-evaluacion-medica-progreso.component';

describe('CrearEditarEvaluacionMedicaProgresoComponent', () => {
  let component: CrearEditarEvaluacionMedicaProgresoComponent;
  let fixture: ComponentFixture<CrearEditarEvaluacionMedicaProgresoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CrearEditarEvaluacionMedicaProgresoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CrearEditarEvaluacionMedicaProgresoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
