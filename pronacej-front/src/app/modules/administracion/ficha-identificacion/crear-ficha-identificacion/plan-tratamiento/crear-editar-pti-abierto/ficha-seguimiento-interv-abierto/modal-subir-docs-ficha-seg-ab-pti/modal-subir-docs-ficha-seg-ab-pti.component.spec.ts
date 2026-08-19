import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModalSubirDocsFichaSegAbPtiComponent } from './modal-subir-docs-ficha-seg-ab-pti.component';

describe('ModalSubirDocsFichaSegAbPtiComponent', () => {
  let component: ModalSubirDocsFichaSegAbPtiComponent;
  let fixture: ComponentFixture<ModalSubirDocsFichaSegAbPtiComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModalSubirDocsFichaSegAbPtiComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ModalSubirDocsFichaSegAbPtiComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
