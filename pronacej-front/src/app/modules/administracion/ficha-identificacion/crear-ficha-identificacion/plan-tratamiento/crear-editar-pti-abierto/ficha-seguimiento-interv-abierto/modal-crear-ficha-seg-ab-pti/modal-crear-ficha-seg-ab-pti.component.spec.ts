import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModalCrearFichaSegAbPtiComponent } from './modal-crear-ficha-seg-ab-pti.component';

describe('ModalCrearFichaSegAbPtiComponent', () => {
  let component: ModalCrearFichaSegAbPtiComponent;
  let fixture: ComponentFixture<ModalCrearFichaSegAbPtiComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModalCrearFichaSegAbPtiComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ModalCrearFichaSegAbPtiComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
