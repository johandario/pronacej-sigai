import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModalCrearEditarCriteriosSeguimientoComponent } from './modal-crear-editar-criterios-seguimiento.component';

describe('ModalCrearEditarCriteriosSeguimientoComponent', () => {
  let component: ModalCrearEditarCriteriosSeguimientoComponent;
  let fixture: ComponentFixture<ModalCrearEditarCriteriosSeguimientoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModalCrearEditarCriteriosSeguimientoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ModalCrearEditarCriteriosSeguimientoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
