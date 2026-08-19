import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModalMostrarDocsSeguiPtiComponent } from '../modal-mostrar-docs-ficha-seg-ab-pti/modal-mostrar-docs-ficha-seg-ab-pti.component';

describe('ModalMostrarDocsSeguiPtiComponent', () => {
  let component: ModalMostrarDocsSeguiPtiComponent;
  let fixture: ComponentFixture<ModalMostrarDocsSeguiPtiComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModalMostrarDocsSeguiPtiComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ModalMostrarDocsSeguiPtiComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
