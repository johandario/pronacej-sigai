import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PtiAbiertoAmonestacionComponent } from './pti-abierto-amonestacion.component';

describe('PtiAbiertoAmonestacionComponent', () => {
  let component: PtiAbiertoAmonestacionComponent;
  let fixture: ComponentFixture<PtiAbiertoAmonestacionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PtiAbiertoAmonestacionComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PtiAbiertoAmonestacionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
