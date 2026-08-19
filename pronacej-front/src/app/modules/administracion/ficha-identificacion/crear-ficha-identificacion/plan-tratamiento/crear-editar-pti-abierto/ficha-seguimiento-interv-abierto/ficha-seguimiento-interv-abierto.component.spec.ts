import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FichaSeguimientoIntervAbiertoComponent } from './ficha-seguimiento-interv-abierto.component';

describe('FichaSeguimientoIntervAbiertoComponent', () => {
  let component: FichaSeguimientoIntervAbiertoComponent;
  let fixture: ComponentFixture<FichaSeguimientoIntervAbiertoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FichaSeguimientoIntervAbiertoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FichaSeguimientoIntervAbiertoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
