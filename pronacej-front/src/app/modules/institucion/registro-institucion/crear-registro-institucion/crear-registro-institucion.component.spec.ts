import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CrearRegistroInstitucionComponent } from './crear-registro-institucion.component';

describe('CrearRegistroInstitucionComponent', () => {
  let component: CrearRegistroInstitucionComponent;
  let fixture: ComponentFixture<CrearRegistroInstitucionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CrearRegistroInstitucionComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CrearRegistroInstitucionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
