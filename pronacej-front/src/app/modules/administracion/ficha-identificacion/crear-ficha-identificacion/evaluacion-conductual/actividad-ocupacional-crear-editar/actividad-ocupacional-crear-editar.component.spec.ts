import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ActividadOcupacionalCrearEditarComponent } from './actividad-ocupacional-crear-editar.component';

describe('ActividadOcupacionalCrearEditarComponent', () => {
  let component: ActividadOcupacionalCrearEditarComponent;
  let fixture: ComponentFixture<ActividadOcupacionalCrearEditarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ActividadOcupacionalCrearEditarComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ActividadOcupacionalCrearEditarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
