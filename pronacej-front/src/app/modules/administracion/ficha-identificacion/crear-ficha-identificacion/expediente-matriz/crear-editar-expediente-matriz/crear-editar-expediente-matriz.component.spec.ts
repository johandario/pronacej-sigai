import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CrearEditarExpedienteMatrizComponent } from './crear-editar-expediente-matriz.component';

describe('CrearEditarExpedienteMatrizComponent', () => {
  let component: CrearEditarExpedienteMatrizComponent;
  let fixture: ComponentFixture<CrearEditarExpedienteMatrizComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CrearEditarExpedienteMatrizComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CrearEditarExpedienteMatrizComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
