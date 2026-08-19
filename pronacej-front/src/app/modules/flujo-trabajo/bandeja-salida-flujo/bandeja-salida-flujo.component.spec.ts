import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BandejaSalidaFlujoComponent } from './bandeja-salida-flujo.component';

describe('BandejaSalidaFlujoComponent', () => {
  let component: BandejaSalidaFlujoComponent;
  let fixture: ComponentFixture<BandejaSalidaFlujoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BandejaSalidaFlujoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BandejaSalidaFlujoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
