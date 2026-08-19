import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FichaIngresoCrearEditarComponent } from './ficha-ingreso-crear-editar.component';

describe('FichaIngresoCrearEditarComponent', () => {
  let component: FichaIngresoCrearEditarComponent;
  let fixture: ComponentFixture<FichaIngresoCrearEditarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FichaIngresoCrearEditarComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FichaIngresoCrearEditarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
