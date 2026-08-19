import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BusquedaAdolescenteDialogComponent } from './busqueda-adolescente-dialog.component';

describe('BusquedaAdolescenteDialogComponent', () => {
  let component: BusquedaAdolescenteDialogComponent;
  let fixture: ComponentFixture<BusquedaAdolescenteDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BusquedaAdolescenteDialogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BusquedaAdolescenteDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
