import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModalInformeFinalAsistenciaComponent } from './modal-informe-final-asistencia.component';

describe('ModalInformeFinalAsistenciaComponent', () => {
  let component: ModalInformeFinalAsistenciaComponent;
  let fixture: ComponentFixture<ModalInformeFinalAsistenciaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModalInformeFinalAsistenciaComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ModalInformeFinalAsistenciaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
