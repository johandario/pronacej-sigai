import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CrearEditarConsultaMedicaComponent } from './crear-editar-consulta-medica.component';

describe('CrearEditarConsultaMedicaComponent', () => {
  let component: CrearEditarConsultaMedicaComponent;
  let fixture: ComponentFixture<CrearEditarConsultaMedicaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CrearEditarConsultaMedicaComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CrearEditarConsultaMedicaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
