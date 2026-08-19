import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListarRegistroInstitucionComponent } from './listar-registro-institucion.component';

describe('ListarRegistroInstitucionComponent', () => {
  let component: ListarRegistroInstitucionComponent;
  let fixture: ComponentFixture<ListarRegistroInstitucionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ListarRegistroInstitucionComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ListarRegistroInstitucionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
