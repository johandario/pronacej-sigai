import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EvaluacionMedicaComponent } from './evaluacion-medica.component';

describe('EvaluacionMedicaComponent', () => {
  let component: EvaluacionMedicaComponent;
  let fixture: ComponentFixture<EvaluacionMedicaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EvaluacionMedicaComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EvaluacionMedicaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
