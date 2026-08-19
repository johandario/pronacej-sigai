import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FichaUbicacionCrearEditarComponent } from './ficha-ubicacion-crear-editar.component';

describe('FichaUbicacionCrearEditarComponent', () => {
  let component: FichaUbicacionCrearEditarComponent;
  let fixture: ComponentFixture<FichaUbicacionCrearEditarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FichaUbicacionCrearEditarComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FichaUbicacionCrearEditarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
