import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FlujoProcesoComponent } from './flujo-proceso.component';

describe('FlujoProcesoComponent', () => {
  let component: FlujoProcesoComponent;
  let fixture: ComponentFixture<FlujoProcesoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FlujoProcesoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FlujoProcesoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
