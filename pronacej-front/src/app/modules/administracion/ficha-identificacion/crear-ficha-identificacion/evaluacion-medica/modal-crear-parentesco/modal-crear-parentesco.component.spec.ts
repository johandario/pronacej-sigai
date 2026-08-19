import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModalCrearParentescoComponent } from './modal-crear-parentesco.component';

describe('ModalCrearParentescoComponent', () => {
  let component: ModalCrearParentescoComponent;
  let fixture: ComponentFixture<ModalCrearParentescoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModalCrearParentescoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ModalCrearParentescoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
