import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CrearEditarMenuPermisoComponent } from './crear-editar-menu-permiso.component';

describe('CrearEditarMenuPermisoComponent', () => {
  let component: CrearEditarMenuPermisoComponent;
  let fixture: ComponentFixture<CrearEditarMenuPermisoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CrearEditarMenuPermisoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CrearEditarMenuPermisoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
