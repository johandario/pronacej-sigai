import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SeleccionarUbigeoComponent } from './seleccionar-ubigeo.component';

describe('SeleccionarUbigeoComponent', () => {
  let component: SeleccionarUbigeoComponent;
  let fixture: ComponentFixture<SeleccionarUbigeoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SeleccionarUbigeoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SeleccionarUbigeoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
