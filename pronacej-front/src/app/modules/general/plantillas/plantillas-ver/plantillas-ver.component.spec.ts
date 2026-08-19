import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PlantillasVerComponent } from './plantillas-ver.component';

describe('PlantillasVerComponent', () => {
  let component: PlantillasVerComponent;
  let fixture: ComponentFixture<PlantillasVerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PlantillasVerComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PlantillasVerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
