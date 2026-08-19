import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CrearGestionInstitucionComponent } from './crear-gestion-institucion.component';

describe('CrearGestionInstitucionComponent', () => {
  let component: CrearGestionInstitucionComponent;
  let fixture: ComponentFixture<CrearGestionInstitucionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CrearGestionInstitucionComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CrearGestionInstitucionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
