import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ContactoAdolescenteCrearEditarComponent } from './contacto-adolescente-crear-editar.component';

describe('ContactoAdolescenteCrearEditarComponent', () => {
  let component: ContactoAdolescenteCrearEditarComponent;
  let fixture: ComponentFixture<ContactoAdolescenteCrearEditarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ContactoAdolescenteCrearEditarComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ContactoAdolescenteCrearEditarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
