import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CrearFichaIdentificacionComponent } from './crear-ficha-identificacion.component';

describe('CrearFichaIdentificacionComponent', () => {
  let component: CrearFichaIdentificacionComponent;
  let fixture: ComponentFixture<CrearFichaIdentificacionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CrearFichaIdentificacionComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CrearFichaIdentificacionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
