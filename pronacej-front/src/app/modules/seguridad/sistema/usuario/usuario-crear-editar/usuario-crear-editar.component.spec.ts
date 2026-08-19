import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UsuarioCrearEditarComponent } from './usuario-crear-editar.component';

describe('UsuarioCrearEditarComponent', () => {
  let component: UsuarioCrearEditarComponent;
  let fixture: ComponentFixture<UsuarioCrearEditarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UsuarioCrearEditarComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UsuarioCrearEditarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
