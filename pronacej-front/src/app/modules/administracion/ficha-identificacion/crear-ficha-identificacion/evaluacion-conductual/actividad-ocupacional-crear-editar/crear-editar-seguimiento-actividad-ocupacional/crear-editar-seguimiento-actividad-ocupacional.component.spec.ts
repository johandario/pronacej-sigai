import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CrearEditarSeguimientoActividadOcupacionalComponent } from './crear-editar-seguimiento-actividad-ocupacional.component';

describe('CrearEditarSeguimientoActividadOcupacionalComponent', () => {
  let component: CrearEditarSeguimientoActividadOcupacionalComponent;
  let fixture: ComponentFixture<CrearEditarSeguimientoActividadOcupacionalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CrearEditarSeguimientoActividadOcupacionalComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CrearEditarSeguimientoActividadOcupacionalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
