import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModalSegPtiComponent } from './modal-seg-pti.component';

describe('ModalSegPtiComponent', () => {
  let component: ModalSegPtiComponent;
  let fixture: ComponentFixture<ModalSegPtiComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModalSegPtiComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ModalSegPtiComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
