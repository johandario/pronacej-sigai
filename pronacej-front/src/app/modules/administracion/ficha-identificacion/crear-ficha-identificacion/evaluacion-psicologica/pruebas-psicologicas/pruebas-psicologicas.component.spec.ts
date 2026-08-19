import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EvaluacionesPsicologicasComponent } from './pruebas-psicologicas.component';

describe('EvaluacionesPsicologicasComponent', () => {
  let component: EvaluacionesPsicologicasComponent;
  let fixture: ComponentFixture<EvaluacionesPsicologicasComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EvaluacionesPsicologicasComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EvaluacionesPsicologicasComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
