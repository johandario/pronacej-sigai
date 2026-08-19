import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EvaluacionConductualVerComponent } from './evaluacion-conductual-ver.component';

describe('EvaluacionConductualVerComponent', () => {
  let component: EvaluacionConductualVerComponent;
  let fixture: ComponentFixture<EvaluacionConductualVerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EvaluacionConductualVerComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EvaluacionConductualVerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
