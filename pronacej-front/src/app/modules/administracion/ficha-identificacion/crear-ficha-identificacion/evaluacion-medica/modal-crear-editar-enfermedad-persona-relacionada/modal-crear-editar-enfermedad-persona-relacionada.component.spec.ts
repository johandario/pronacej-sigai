import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModalCrearEditarEnfermedadPersonaRelacionadaComponent } from './modal-crear-editar-enfermedad-persona-relacionada.component';

describe('ModalCrearEditarEnfermedadPersonaRelacionadaComponent', () => {
  let component: ModalCrearEditarEnfermedadPersonaRelacionadaComponent;
  let fixture: ComponentFixture<ModalCrearEditarEnfermedadPersonaRelacionadaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModalCrearEditarEnfermedadPersonaRelacionadaComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ModalCrearEditarEnfermedadPersonaRelacionadaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
