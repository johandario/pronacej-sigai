import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModalEditaIntervAbiertoComponent } from './modal-edita-interv-abierto.component';

describe('ModalEditaIntervAbiertoComponent', () => {
  let component: ModalEditaIntervAbiertoComponent;
  let fixture: ComponentFixture<ModalEditaIntervAbiertoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModalEditaIntervAbiertoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ModalEditaIntervAbiertoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
