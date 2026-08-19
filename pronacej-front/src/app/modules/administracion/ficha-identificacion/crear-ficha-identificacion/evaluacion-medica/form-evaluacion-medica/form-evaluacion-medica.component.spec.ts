import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FormEvaluacionMedicaComponent } from './form-evaluacion-medica.component';

describe('FormEvaluacionMedicaComponent', () => {
  let component: FormEvaluacionMedicaComponent;
  let fixture: ComponentFixture<FormEvaluacionMedicaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FormEvaluacionMedicaComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FormEvaluacionMedicaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
