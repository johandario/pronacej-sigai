import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EvaluacionDomiciliariaCrearEditarComponent } from './evaluacion-domiciliaria-crear-editar.component';

describe('EvaluacionDomiciliariaCrearEditarComponent', () => {
  let component: EvaluacionDomiciliariaCrearEditarComponent;
  let fixture: ComponentFixture<EvaluacionDomiciliariaCrearEditarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EvaluacionDomiciliariaCrearEditarComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EvaluacionDomiciliariaCrearEditarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
