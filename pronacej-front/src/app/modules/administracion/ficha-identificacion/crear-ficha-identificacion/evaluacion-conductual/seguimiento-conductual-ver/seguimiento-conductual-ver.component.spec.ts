import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SeguimientoConductualVerComponent } from './seguimiento-conductual-ver.component';

describe('SeguimientoConductualVerComponent', () => {
  let component: SeguimientoConductualVerComponent;
  let fixture: ComponentFixture<SeguimientoConductualVerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SeguimientoConductualVerComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SeguimientoConductualVerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
