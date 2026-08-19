import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EvaluacionDomiciliariaComponent } from './evaluacion-domiciliaria.component';

describe('EvaluacionDomiciliariaComponent', () => {
  let component: EvaluacionDomiciliariaComponent;
  let fixture: ComponentFixture<EvaluacionDomiciliariaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EvaluacionDomiciliariaComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EvaluacionDomiciliariaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
