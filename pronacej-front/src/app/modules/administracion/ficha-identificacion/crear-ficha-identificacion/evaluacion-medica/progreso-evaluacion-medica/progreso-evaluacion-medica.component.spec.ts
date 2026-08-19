import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProgresoEvaluacionMedicaComponent } from './progreso-evaluacion-medica.component';

describe('ProgresoEvaluacionMedicaComponent', () => {
  let component: ProgresoEvaluacionMedicaComponent;
  let fixture: ComponentFixture<ProgresoEvaluacionMedicaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProgresoEvaluacionMedicaComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ProgresoEvaluacionMedicaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
