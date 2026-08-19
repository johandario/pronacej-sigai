import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DocsEvaluacionMedicaComponent } from './docs-evaluacion-medica.component';

describe('DocsEvaluacionMedicaComponent', () => {
  let component: DocsEvaluacionMedicaComponent;
  let fixture: ComponentFixture<DocsEvaluacionMedicaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DocsEvaluacionMedicaComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DocsEvaluacionMedicaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
