import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListarGestionInstitucionComponent } from './listar-gestion-institucion.component';

describe('ListarGestionInstitucionComponent', () => {
  let component: ListarGestionInstitucionComponent;
  let fixture: ComponentFixture<ListarGestionInstitucionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ListarGestionInstitucionComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ListarGestionInstitucionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
