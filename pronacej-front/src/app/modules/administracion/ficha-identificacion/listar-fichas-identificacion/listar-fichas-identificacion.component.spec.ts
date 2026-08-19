import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListarFichasIdentificacionComponent } from './listar-fichas-identificacion.component';

describe('ListarFichasIdentificacionComponent', () => {
  let component: ListarFichasIdentificacionComponent;
  let fixture: ComponentFixture<ListarFichasIdentificacionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ListarFichasIdentificacionComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ListarFichasIdentificacionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
