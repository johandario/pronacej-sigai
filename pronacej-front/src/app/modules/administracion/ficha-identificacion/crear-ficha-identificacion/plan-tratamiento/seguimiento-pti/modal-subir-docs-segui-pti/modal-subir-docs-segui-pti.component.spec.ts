import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModalSubirDocsSeguiPtiComponent } from '../modal-subir-docs-ficha-seg-ab-pti/modal-subir-docs-ficha-seg-ab-pti.component';

describe('ModalSubirDocsSeguiPtiComponent', () => {
  let component: ModalSubirDocsSeguiPtiComponent;
  let fixture: ComponentFixture<ModalSubirDocsSeguiPtiComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModalSubirDocsSeguiPtiComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ModalSubirDocsSeguiPtiComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
