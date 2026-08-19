import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListarAdolescenteDerivadoComponent } from './listar-adolescente-derivado.component';

describe('ListarAdolescenteDerivadoComponent', () => {
  let component: ListarAdolescenteDerivadoComponent;
  let fixture: ComponentFixture<ListarAdolescenteDerivadoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ListarAdolescenteDerivadoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ListarAdolescenteDerivadoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
