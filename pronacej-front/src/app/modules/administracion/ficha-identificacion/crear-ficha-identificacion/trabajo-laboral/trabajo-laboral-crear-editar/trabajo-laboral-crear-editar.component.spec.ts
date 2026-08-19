import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TrabajoLaboralCrearEditarComponent } from './trabajo-laboral-crear-editar.component';

describe('TrabajoLaboralCrearEditarComponent', () => {
  let component: TrabajoLaboralCrearEditarComponent;
  let fixture: ComponentFixture<TrabajoLaboralCrearEditarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TrabajoLaboralCrearEditarComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TrabajoLaboralCrearEditarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
