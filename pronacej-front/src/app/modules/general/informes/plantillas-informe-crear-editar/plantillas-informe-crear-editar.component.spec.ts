import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PlantillasInformeCrearEditarComponent } from './plantillas-informe-crear-editar.component';

describe('PlantillasInformeCrearEditarComponent', () => {
  let component: PlantillasInformeCrearEditarComponent;
  let fixture: ComponentFixture<PlantillasInformeCrearEditarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PlantillasInformeCrearEditarComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PlantillasInformeCrearEditarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
