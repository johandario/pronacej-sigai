import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModalCrearDetalleRecetaComponent } from './modal-crear-detalle-receta.component';

describe('ModalCrearDetalleRecetaComponent', () => {
  let component: ModalCrearDetalleRecetaComponent;
  let fixture: ComponentFixture<ModalCrearDetalleRecetaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModalCrearDetalleRecetaComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ModalCrearDetalleRecetaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
