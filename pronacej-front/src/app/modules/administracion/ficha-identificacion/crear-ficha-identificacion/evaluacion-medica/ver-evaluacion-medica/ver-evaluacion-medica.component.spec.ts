import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VerEvaluacionMedicaComponent } from './ver-evaluacion-medica.component';

describe('VerEvaluacionMedicaComponent', () => {
  let component: VerEvaluacionMedicaComponent;
  let fixture: ComponentFixture<VerEvaluacionMedicaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VerEvaluacionMedicaComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(VerEvaluacionMedicaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
