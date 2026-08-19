import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModalSegSubactPtiComponent } from './modal-seg-subact-pti.component';

describe('ModalSegSubactPtiComponent', () => {
  let component: ModalSegSubactPtiComponent;
  let fixture: ComponentFixture<ModalSegSubactPtiComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModalSegSubactPtiComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ModalSegSubactPtiComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
