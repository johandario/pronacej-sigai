import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VisualizarExpedienteMatrizComponent } from './visualizar-expediente-matriz.component';

describe('VisualizarExpedienteMatrizComponent', () => {
  let component: VisualizarExpedienteMatrizComponent;
  let fixture: ComponentFixture<VisualizarExpedienteMatrizComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VisualizarExpedienteMatrizComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(VisualizarExpedienteMatrizComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
