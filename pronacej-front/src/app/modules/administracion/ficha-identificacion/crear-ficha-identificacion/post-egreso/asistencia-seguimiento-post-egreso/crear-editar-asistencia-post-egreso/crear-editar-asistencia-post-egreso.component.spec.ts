import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CrearEditarAsistenciaPostEgresoComponent } from './crear-editar-asistencia-post-egreso.component';

describe('CrearEditarAsistenciaPostEgresoComponent', () => {
  let component: CrearEditarAsistenciaPostEgresoComponent;
  let fixture: ComponentFixture<CrearEditarAsistenciaPostEgresoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CrearEditarAsistenciaPostEgresoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CrearEditarAsistenciaPostEgresoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
