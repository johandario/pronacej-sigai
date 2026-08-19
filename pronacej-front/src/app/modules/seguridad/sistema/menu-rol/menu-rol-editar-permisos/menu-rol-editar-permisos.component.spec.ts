import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MenuRolEditarPermisosComponent } from './menu-rol-editar-permisos.component';

describe('MenuRolEditarPermisosComponent', () => {
  let component: MenuRolEditarPermisosComponent;
  let fixture: ComponentFixture<MenuRolEditarPermisosComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MenuRolEditarPermisosComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MenuRolEditarPermisosComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
