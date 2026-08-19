import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListarSalidaPermisoComponent } from './listar-salida-permiso.component';

describe('ListarSalidaPermisoComponent', () => {
  let component: ListarSalidaPermisoComponent;
  let fixture: ComponentFixture<ListarSalidaPermisoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ListarSalidaPermisoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ListarSalidaPermisoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
