import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SeguimientoPsicologicoCrearEditarComponent } from './seguimiento-psicologico-crear-editar.component';

describe('SeguimientoPsicologicoCrearEditarComponent', () => {
  let component: SeguimientoPsicologicoCrearEditarComponent;
  let fixture: ComponentFixture<SeguimientoPsicologicoCrearEditarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SeguimientoPsicologicoCrearEditarComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SeguimientoPsicologicoCrearEditarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
