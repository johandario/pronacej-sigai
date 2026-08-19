import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TrabajoLaboralComponent } from './trabajo-laboral.component';

describe('TrabajoLaboralComponent', () => {
  let component: TrabajoLaboralComponent;
  let fixture: ComponentFixture<TrabajoLaboralComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TrabajoLaboralComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TrabajoLaboralComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
