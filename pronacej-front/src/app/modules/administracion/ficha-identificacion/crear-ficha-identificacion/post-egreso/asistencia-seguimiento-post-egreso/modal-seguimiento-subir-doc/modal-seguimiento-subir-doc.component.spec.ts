import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModalSeguimientoSubirDocComponent } from './modal-seguimiento-subir-doc.component';

describe('ModalSeguimientoSubirDocComponent', () => {
  let component: ModalSeguimientoSubirDocComponent;
  let fixture: ComponentFixture<ModalSeguimientoSubirDocComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModalSeguimientoSubirDocComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ModalSeguimientoSubirDocComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
