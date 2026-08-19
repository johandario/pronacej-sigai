import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RegistroIngresoComponent } from './registro-ingreso.component';

describe('RegistroIngresoComponent', () => {
  let component: RegistroIngresoComponent;
  let fixture: ComponentFixture<RegistroIngresoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RegistroIngresoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RegistroIngresoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
