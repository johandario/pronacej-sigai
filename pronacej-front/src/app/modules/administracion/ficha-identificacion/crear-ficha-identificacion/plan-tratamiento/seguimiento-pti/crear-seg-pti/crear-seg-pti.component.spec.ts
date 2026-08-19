import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CrearSegPtiComponent } from './crear-seg-pti.component';

describe('CrearSegPtiComponent', () => {
  let component: CrearSegPtiComponent;
  let fixture: ComponentFixture<CrearSegPtiComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CrearSegPtiComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CrearSegPtiComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
