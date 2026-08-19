import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModalEditaIntervComponent } from './modal-edita-interv.component';

describe('ModalEditaIntervComponent', () => {
  let component: ModalEditaIntervComponent;
  let fixture: ComponentFixture<ModalEditaIntervComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModalEditaIntervComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ModalEditaIntervComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
