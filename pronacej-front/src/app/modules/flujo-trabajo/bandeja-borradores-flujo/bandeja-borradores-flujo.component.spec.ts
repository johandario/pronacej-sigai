import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BandejaBorradoresFlujoComponent } from './bandeja-borradores-flujo.component';

describe('BandejaBorradoresFlujoComponent', () => {
  let component: BandejaBorradoresFlujoComponent;
  let fixture: ComponentFixture<BandejaBorradoresFlujoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BandejaBorradoresFlujoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BandejaBorradoresFlujoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
