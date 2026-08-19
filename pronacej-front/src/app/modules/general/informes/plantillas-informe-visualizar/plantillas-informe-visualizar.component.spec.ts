import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PlantillasInformeVisualizarComponent } from './plantillas-informe-visualizar.component';

describe('PlantillasInformeVisualizarComponent', () => {
  let component: PlantillasInformeVisualizarComponent;
  let fixture: ComponentFixture<PlantillasInformeVisualizarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PlantillasInformeVisualizarComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PlantillasInformeVisualizarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
