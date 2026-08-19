import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListadoRegistrosIngresoComponent } from './listado-registros-ingreso.component';

describe('ListadoRegistrosIngresoComponent', () => {
  let component: ListadoRegistrosIngresoComponent;
  let fixture: ComponentFixture<ListadoRegistrosIngresoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ListadoRegistrosIngresoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ListadoRegistrosIngresoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
