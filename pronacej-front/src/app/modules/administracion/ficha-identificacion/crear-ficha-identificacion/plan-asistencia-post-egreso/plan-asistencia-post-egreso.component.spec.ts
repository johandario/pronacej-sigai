import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PlanAsistenciaPostEgresoComponent } from './plan-asistencia-post-egreso.component';

describe('PlanAsistenciaPostEgresoComponent', () => {
  let component: PlanAsistenciaPostEgresoComponent;
  let fixture: ComponentFixture<PlanAsistenciaPostEgresoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PlanAsistenciaPostEgresoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PlanAsistenciaPostEgresoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
