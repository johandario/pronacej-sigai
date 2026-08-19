import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CrearEvaluacionMedicaComponent } from './crear-evaluacion-medica.component';

describe('CrearEvaluacionMedicaComponent', () => {
  let component: CrearEvaluacionMedicaComponent;
  let fixture: ComponentFixture<CrearEvaluacionMedicaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CrearEvaluacionMedicaComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CrearEvaluacionMedicaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
