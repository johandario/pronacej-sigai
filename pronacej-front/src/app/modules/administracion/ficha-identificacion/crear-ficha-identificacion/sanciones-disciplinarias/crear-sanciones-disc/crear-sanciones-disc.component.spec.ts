import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CrearSancionesDiscComponent } from './crear-sanciones-disc.component';

describe('CrearSancionesDiscComponent', () => {
  let component: CrearSancionesDiscComponent;
  let fixture: ComponentFixture<CrearSancionesDiscComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CrearSancionesDiscComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CrearSancionesDiscComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
