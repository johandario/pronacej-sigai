import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModalCrearEditarDetalleAsistenciaComponent } from './modal-crear-editar-detalle-asistencia.component';

describe('ModalCrearEditarDetalleAsistenciaComponent', () => {
  let component: ModalCrearEditarDetalleAsistenciaComponent;
  let fixture: ComponentFixture<ModalCrearEditarDetalleAsistenciaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModalCrearEditarDetalleAsistenciaComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ModalCrearEditarDetalleAsistenciaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
