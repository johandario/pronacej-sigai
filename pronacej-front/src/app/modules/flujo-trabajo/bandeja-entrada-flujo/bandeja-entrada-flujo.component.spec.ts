import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BandejaEntradaFlujoComponent } from './bandeja-entrada-flujo.component';

describe('BandejaEntradaFlujoComponent', () => {
  let component: BandejaEntradaFlujoComponent;
  let fixture: ComponentFixture<BandejaEntradaFlujoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BandejaEntradaFlujoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BandejaEntradaFlujoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
