import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DialogSeleccionarJerarquiaComponent } from './dialog-seleccionar-jerarquia.component';

describe('DialogSeleccionarJerarquiaComponent', () => {
  let component: DialogSeleccionarJerarquiaComponent;
  let fixture: ComponentFixture<DialogSeleccionarJerarquiaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DialogSeleccionarJerarquiaComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DialogSeleccionarJerarquiaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
