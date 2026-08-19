import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModalEditaVariableComponent } from './modal-edita-variable.component';

describe('ModalEditaVariableComponent', () => {
  let component: ModalEditaVariableComponent;
  let fixture: ComponentFixture<ModalEditaVariableComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModalEditaVariableComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ModalEditaVariableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
