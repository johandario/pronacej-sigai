import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CrearPermisoSalidaComponent } from './crear-permiso-salida.component';

describe('CrearPermisoSalidaComponent', () => {
  let component: CrearPermisoSalidaComponent;
  let fixture: ComponentFixture<CrearPermisoSalidaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CrearPermisoSalidaComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CrearPermisoSalidaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
