import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModalSegPtiAbiertoComponent } from './modal-seg-pti-abierto.component';

describe('ModalSegPtiAbiertoComponent', () => {
  let component: ModalSegPtiAbiertoComponent;
  let fixture: ComponentFixture<ModalSegPtiAbiertoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModalSegPtiAbiertoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ModalSegPtiAbiertoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
