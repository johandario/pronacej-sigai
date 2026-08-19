import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FichaIngresoComponent } from './ficha-ingreso.component';

describe('FichaIngresoComponent', () => {
  let component: FichaIngresoComponent;
  let fixture: ComponentFixture<FichaIngresoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FichaIngresoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FichaIngresoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
