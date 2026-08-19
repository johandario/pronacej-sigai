import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FlujoInstanciaComponent } from './flujo-instancia.component';

describe('FlujoInstanciaComponent', () => {
  let component: FlujoInstanciaComponent;
  let fixture: ComponentFixture<FlujoInstanciaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FlujoInstanciaComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FlujoInstanciaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
