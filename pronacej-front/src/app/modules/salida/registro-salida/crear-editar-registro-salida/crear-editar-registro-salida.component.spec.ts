import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CrearEditarRegistroSalidaComponent } from './crear-editar-registro-salida.component';

describe('CrearEditarRegistroSalidaComponent', () => {
  let component: CrearEditarRegistroSalidaComponent;
  let fixture: ComponentFixture<CrearEditarRegistroSalidaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CrearEditarRegistroSalidaComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CrearEditarRegistroSalidaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
