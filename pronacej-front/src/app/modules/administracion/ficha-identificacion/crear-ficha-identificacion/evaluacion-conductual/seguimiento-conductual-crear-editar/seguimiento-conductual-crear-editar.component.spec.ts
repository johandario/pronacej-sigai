import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SeguimientoConductualCrearEditarComponent } from './seguimiento-conductual-crear-editar.component';

describe('SeguimientoConductualCrearEditarComponent', () => {
  let component: SeguimientoConductualCrearEditarComponent;
  let fixture: ComponentFixture<SeguimientoConductualCrearEditarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SeguimientoConductualCrearEditarComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SeguimientoConductualCrearEditarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
