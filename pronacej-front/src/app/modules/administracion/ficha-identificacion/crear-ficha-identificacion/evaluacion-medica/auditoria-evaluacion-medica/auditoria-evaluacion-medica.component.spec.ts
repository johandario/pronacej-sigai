import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AuditoriaEvaluacionMedicaComponent } from './auditoria-evaluacion-medica.component';

describe('AuditoriaEvaluacionMedicaComponent', () => {
  let component: AuditoriaEvaluacionMedicaComponent;
  let fixture: ComponentFixture<AuditoriaEvaluacionMedicaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AuditoriaEvaluacionMedicaComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AuditoriaEvaluacionMedicaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
