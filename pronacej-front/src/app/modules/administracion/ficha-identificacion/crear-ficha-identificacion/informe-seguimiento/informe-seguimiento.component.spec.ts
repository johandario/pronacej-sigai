import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InformeSeguimientoComponent } from './informe-seguimiento.component';

describe('InformeSeguimientoComponent', () => {
  let component: InformeSeguimientoComponent;
  let fixture: ComponentFixture<InformeSeguimientoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InformeSeguimientoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(InformeSeguimientoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
