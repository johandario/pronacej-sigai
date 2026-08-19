import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModalCrearEditarEnfermedadFichaComponent } from './modal-crear-editar-enfermedad-ficha.component';

describe('ModalCrearEditarEnfermedadFichaComponent', () => {
  let component: ModalCrearEditarEnfermedadFichaComponent;
  let fixture: ComponentFixture<ModalCrearEditarEnfermedadFichaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModalCrearEditarEnfermedadFichaComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ModalCrearEditarEnfermedadFichaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
