import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModalCrearDetalleOrdenComponent } from './modal-crear-detalle-orden.component';

describe('ModalCrearDetalleOrdenComponent', () => {
  let component: ModalCrearDetalleOrdenComponent;
  let fixture: ComponentFixture<ModalCrearDetalleOrdenComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModalCrearDetalleOrdenComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ModalCrearDetalleOrdenComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
