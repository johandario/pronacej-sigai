import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModalCrearInfFinalAbiertoComponent } from './modal-crear-inf-final-abierto.component';

describe('ModalCrearInfFinalAbiertoComponent', () => {
  let component: ModalCrearInfFinalAbiertoComponent;
  let fixture: ComponentFixture<ModalCrearInfFinalAbiertoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModalCrearInfFinalAbiertoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ModalCrearInfFinalAbiertoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
