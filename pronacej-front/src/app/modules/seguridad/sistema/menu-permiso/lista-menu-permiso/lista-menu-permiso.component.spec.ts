import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListaMenuPermisoComponent } from './lista-menu-permiso.component';

describe('ListaMenuPermisoComponent', () => {
  let component: ListaMenuPermisoComponent;
  let fixture: ComponentFixture<ListaMenuPermisoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ListaMenuPermisoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ListaMenuPermisoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
