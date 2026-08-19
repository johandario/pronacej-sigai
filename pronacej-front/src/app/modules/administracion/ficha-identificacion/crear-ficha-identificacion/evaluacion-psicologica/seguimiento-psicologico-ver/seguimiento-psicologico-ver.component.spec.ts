import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SeguimientoPsicologicoVerComponent } from './seguimiento-psicologico-ver.component';

describe('SeguimientoPsicologicoVerComponent', () => {
  let component: SeguimientoPsicologicoVerComponent;
  let fixture: ComponentFixture<SeguimientoPsicologicoVerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SeguimientoPsicologicoVerComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SeguimientoPsicologicoVerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
