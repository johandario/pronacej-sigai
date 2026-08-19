import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModalMostrarDocsFichaSegAbPtiComponent } from './modal-mostrar-docs-ficha-seg-ab-pti.component';

describe('ModalMostrarDocsFichaSegAbPtiComponent', () => {
  let component: ModalMostrarDocsFichaSegAbPtiComponent;
  let fixture: ComponentFixture<ModalMostrarDocsFichaSegAbPtiComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModalMostrarDocsFichaSegAbPtiComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ModalMostrarDocsFichaSegAbPtiComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
