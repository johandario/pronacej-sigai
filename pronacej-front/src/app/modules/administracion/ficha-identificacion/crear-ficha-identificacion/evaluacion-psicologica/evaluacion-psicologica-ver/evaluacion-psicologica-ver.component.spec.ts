import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EvaluacionPsicologicaVerComponent } from './evaluacion-psicologica-ver.component';

describe('EvaluacionPsicologicaVerComponent', () => {
  let component: EvaluacionPsicologicaVerComponent;
  let fixture: ComponentFixture<EvaluacionPsicologicaVerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EvaluacionPsicologicaVerComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EvaluacionPsicologicaVerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
