import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FichaUbicacionComponent } from './ficha-ubicacion.component';

describe('FichaUbicacionComponent', () => {
  let component: FichaUbicacionComponent;
  let fixture: ComponentFixture<FichaUbicacionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FichaUbicacionComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FichaUbicacionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
