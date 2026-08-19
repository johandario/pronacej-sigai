import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UsuariosCrearEditarComponent } from './usuarios-crear-editar.component';

describe('UsuariosCrearEditarComponent', () => {
  let component: UsuariosCrearEditarComponent;
  let fixture: ComponentFixture<UsuariosCrearEditarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UsuariosCrearEditarComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UsuariosCrearEditarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
