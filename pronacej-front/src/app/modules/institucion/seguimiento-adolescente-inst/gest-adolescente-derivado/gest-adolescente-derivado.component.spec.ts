import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GestAdolescenteDerivadoComponent } from './gest-adolescente-derivado.component';

describe('GestAdolescenteDerivadoComponent', () => {
  let component: GestAdolescenteDerivadoComponent;
  let fixture: ComponentFixture<GestAdolescenteDerivadoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GestAdolescenteDerivadoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GestAdolescenteDerivadoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
