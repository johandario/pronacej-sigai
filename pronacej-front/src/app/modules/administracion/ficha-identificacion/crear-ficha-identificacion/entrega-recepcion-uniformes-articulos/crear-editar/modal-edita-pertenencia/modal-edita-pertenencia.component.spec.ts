import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModalEditaPertenenciaComponent } from './modal-edita-pertenencia.component';

describe('ModalEditaPertenenciaComponent', () => {
  let component: ModalEditaPertenenciaComponent;
  let fixture: ComponentFixture<ModalEditaPertenenciaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModalEditaPertenenciaComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ModalEditaPertenenciaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
