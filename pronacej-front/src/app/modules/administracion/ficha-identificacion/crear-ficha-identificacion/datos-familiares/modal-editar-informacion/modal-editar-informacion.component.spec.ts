import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModalEditarInformacionComponent } from './modal-editar-informacion.component';

describe('ModalEditarInformacionComponent', () => {
  let component: ModalEditarInformacionComponent;
  let fixture: ComponentFixture<ModalEditarInformacionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModalEditarInformacionComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ModalEditarInformacionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
