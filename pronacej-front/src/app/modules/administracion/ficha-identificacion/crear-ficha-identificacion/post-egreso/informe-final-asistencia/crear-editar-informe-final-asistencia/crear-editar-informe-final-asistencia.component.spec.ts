import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CrearEditarInformeFinalAsistenciaComponent } from './crear-editar-informe-final-asistencia.component';

describe('CrearEditarInformeFinalAsistenciaComponent', () => {
  let component: CrearEditarInformeFinalAsistenciaComponent;
  let fixture: ComponentFixture<CrearEditarInformeFinalAsistenciaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CrearEditarInformeFinalAsistenciaComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CrearEditarInformeFinalAsistenciaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
