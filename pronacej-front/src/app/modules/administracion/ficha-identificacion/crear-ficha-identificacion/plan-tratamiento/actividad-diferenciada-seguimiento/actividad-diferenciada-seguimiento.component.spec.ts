import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ActividadDiferenciadaSeguimientoComponent } from './actividad-diferenciada-seguimiento.component';

describe('ActividadDiferenciadaSeguimientoComponent', () => {
  let component: ActividadDiferenciadaSeguimientoComponent;
  let fixture: ComponentFixture<ActividadDiferenciadaSeguimientoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ActividadDiferenciadaSeguimientoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ActividadDiferenciadaSeguimientoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
