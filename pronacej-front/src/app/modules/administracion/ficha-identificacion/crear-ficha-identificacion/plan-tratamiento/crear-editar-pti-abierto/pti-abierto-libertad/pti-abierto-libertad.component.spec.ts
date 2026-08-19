import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PtiAbiertoLibertadComponent } from './pti-abierto-libertad.component';

describe('PtiAbiertoLibertadComponent', () => {
  let component: PtiAbiertoLibertadComponent;
  let fixture: ComponentFixture<PtiAbiertoLibertadComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PtiAbiertoLibertadComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PtiAbiertoLibertadComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
