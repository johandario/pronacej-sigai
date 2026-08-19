import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InformeSeguimientoCrearEditarComponent } from './informe-seguimiento-crear-editar.component';

describe('InformeSeguimientoCrearEditarComponent', () => {
  let component: InformeSeguimientoCrearEditarComponent;
  let fixture: ComponentFixture<InformeSeguimientoCrearEditarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InformeSeguimientoCrearEditarComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(InformeSeguimientoCrearEditarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
