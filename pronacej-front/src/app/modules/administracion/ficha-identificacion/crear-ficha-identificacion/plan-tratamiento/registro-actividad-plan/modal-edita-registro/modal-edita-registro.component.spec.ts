import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModalEditaRegistroComponent } from './modal-edita-registro.component';

describe('ModalEditaRegistroComponent', () => {
  let component: ModalEditaRegistroComponent;
  let fixture: ComponentFixture<ModalEditaRegistroComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModalEditaRegistroComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ModalEditaRegistroComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
