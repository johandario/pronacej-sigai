import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CrearSeguimientoAdolescenteComponent } from './crear-seguimiento-adolescente.component';

describe('CrearSeguimientoAdolescenteComponent', () => {
  let component: CrearSeguimientoAdolescenteComponent;
  let fixture: ComponentFixture<CrearSeguimientoAdolescenteComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CrearSeguimientoAdolescenteComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CrearSeguimientoAdolescenteComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
