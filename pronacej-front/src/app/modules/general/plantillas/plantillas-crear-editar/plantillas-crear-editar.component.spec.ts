import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PlantillasCrearEditarComponent } from './plantillas-crear-editar.component';

describe('PlantillasCrearEditarComponent', () => {
  let component: PlantillasCrearEditarComponent;
  let fixture: ComponentFixture<PlantillasCrearEditarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PlantillasCrearEditarComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PlantillasCrearEditarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
