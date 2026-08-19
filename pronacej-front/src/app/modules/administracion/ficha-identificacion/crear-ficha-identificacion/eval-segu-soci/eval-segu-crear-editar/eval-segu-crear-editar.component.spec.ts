import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EvalSeguCrearEditarComponent } from './eval-segu-crear-editar.component';

describe('EvalSeguCrearEditarComponent', () => {
  let component: EvalSeguCrearEditarComponent;
  let fixture: ComponentFixture<EvalSeguCrearEditarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EvalSeguCrearEditarComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EvalSeguCrearEditarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
