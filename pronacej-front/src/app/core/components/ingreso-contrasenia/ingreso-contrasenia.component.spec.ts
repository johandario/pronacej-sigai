import { ComponentFixture, TestBed } from '@angular/core/testing';

import { IngresoContraseniaComponent } from './ingreso-contrasenia.component';

describe('IngresoContraseniaComponent', () => {
  let component: IngresoContraseniaComponent;
  let fixture: ComponentFixture<IngresoContraseniaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [IngresoContraseniaComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(IngresoContraseniaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
