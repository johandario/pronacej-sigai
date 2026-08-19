import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModalPasoComponent } from './modal-paso.component';

describe('ModalPasoComponent', () => {
  let component: ModalPasoComponent;
  let fixture: ComponentFixture<ModalPasoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModalPasoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ModalPasoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
