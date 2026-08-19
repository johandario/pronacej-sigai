import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SegPtiAbiertoComponent } from './seg-pti-abierto.component';

describe('SegPtiAbiertoComponent', () => {
  let component: SegPtiAbiertoComponent;
  let fixture: ComponentFixture<SegPtiAbiertoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SegPtiAbiertoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SegPtiAbiertoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
