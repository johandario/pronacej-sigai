import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ActaExternamientoCrearEditarComponent } from './acta-externamiento-crear-editar.component';

describe('ActaExternamientoCrearEditarComponent', () => {
  let component: ActaExternamientoCrearEditarComponent;
  let fixture: ComponentFixture<ActaExternamientoCrearEditarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ActaExternamientoCrearEditarComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ActaExternamientoCrearEditarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
