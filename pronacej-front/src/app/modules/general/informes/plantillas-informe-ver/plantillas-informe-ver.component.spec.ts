import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PlantillasInformeVerComponent } from './plantillas-informe-ver.component';

describe('PlantillasInformeVerComponent', () => {
  let component: PlantillasInformeVerComponent;
  let fixture: ComponentFixture<PlantillasInformeVerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PlantillasInformeVerComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PlantillasInformeVerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
