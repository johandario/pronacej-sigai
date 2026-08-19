import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SeguimientoEvaluacionMedicaComponent } from './seguimiento-evaluacion-medica.component';

describe('SeguimientoEvaluacionMedicaComponent', () => {
  let component: SeguimientoEvaluacionMedicaComponent;
  let fixture: ComponentFixture<SeguimientoEvaluacionMedicaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SeguimientoEvaluacionMedicaComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SeguimientoEvaluacionMedicaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
