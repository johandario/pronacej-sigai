import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AccionesExpedienteMatrizComponent } from './acciones-expediente-matriz.component';

describe('AccionesExpedienteMatrizComponent', () => {
  let component: AccionesExpedienteMatrizComponent;
  let fixture: ComponentFixture<AccionesExpedienteMatrizComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AccionesExpedienteMatrizComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AccionesExpedienteMatrizComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
