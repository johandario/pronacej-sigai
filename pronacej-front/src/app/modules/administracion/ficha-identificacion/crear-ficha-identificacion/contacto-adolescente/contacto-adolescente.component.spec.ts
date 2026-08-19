import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ContactoAdolescenteComponent } from './contacto-adolescente.component';

describe('ContactoAdolescenteComponent', () => {
  let component: ContactoAdolescenteComponent;
  let fixture: ComponentFixture<ContactoAdolescenteComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ContactoAdolescenteComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ContactoAdolescenteComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
