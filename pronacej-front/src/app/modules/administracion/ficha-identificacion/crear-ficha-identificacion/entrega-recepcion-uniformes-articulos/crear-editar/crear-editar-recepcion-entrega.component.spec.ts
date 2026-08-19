import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CrearEditarRecepcionEntregaComponent } from './crear-editar-recepcion-entrega.component';

describe('CrearEditarRecepcionEntregaComponent', () => {
  let component: CrearEditarRecepcionEntregaComponent;
  let fixture: ComponentFixture<CrearEditarRecepcionEntregaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CrearEditarRecepcionEntregaComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CrearEditarRecepcionEntregaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
