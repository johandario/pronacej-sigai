import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AsistenciaSeguimientoPostEgresoComponent } from './asistencia-seguimiento-post-egreso.component';

describe('AsistenciaSeguimientoPostEgresoComponent', () => {
  let component: AsistenciaSeguimientoPostEgresoComponent;
  let fixture: ComponentFixture<AsistenciaSeguimientoPostEgresoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AsistenciaSeguimientoPostEgresoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AsistenciaSeguimientoPostEgresoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
