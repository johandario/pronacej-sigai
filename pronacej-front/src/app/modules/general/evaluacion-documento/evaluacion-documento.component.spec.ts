import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EvaluacionDocumentoComponent } from './evaluacion-documento.component';

describe('EvaluacionDocumentoComponent', () => {
  let component: EvaluacionDocumentoComponent;
  let fixture: ComponentFixture<EvaluacionDocumentoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EvaluacionDocumentoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EvaluacionDocumentoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
