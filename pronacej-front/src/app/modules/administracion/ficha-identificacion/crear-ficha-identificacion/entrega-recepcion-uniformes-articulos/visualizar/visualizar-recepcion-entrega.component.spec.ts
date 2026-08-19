import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VisualizarRecepcionEntregaComponent } from './visualizar-recepcion-entrega.component';

describe('VisualizarRecepcionEntregaComponent', () => {
  let component: VisualizarRecepcionEntregaComponent;
  let fixture: ComponentFixture<VisualizarRecepcionEntregaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VisualizarRecepcionEntregaComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(VisualizarRecepcionEntregaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
