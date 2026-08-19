import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PtiAbiertoComunidadComponent } from './pti-abierto-comunidad.component';

describe('PtiAbiertoComunidadComponent', () => {
  let component: PtiAbiertoComunidadComponent;
  let fixture: ComponentFixture<PtiAbiertoComunidadComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PtiAbiertoComunidadComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PtiAbiertoComunidadComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
