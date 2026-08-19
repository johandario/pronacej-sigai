import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModalCreacionLocalidadesComponent } from './modal-creacion-localidades.component';

describe('ModalCreacionLocalidadesComponent', () => {
  let component: ModalCreacionLocalidadesComponent;
  let fixture: ComponentFixture<ModalCreacionLocalidadesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModalCreacionLocalidadesComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ModalCreacionLocalidadesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
