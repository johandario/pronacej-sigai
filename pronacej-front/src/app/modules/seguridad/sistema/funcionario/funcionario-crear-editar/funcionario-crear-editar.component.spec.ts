import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FuncionarioCrearEditarComponent } from './funcionario-crear-editar.component';

describe('FuncionarioCrearEditarComponent', () => {
  let component: FuncionarioCrearEditarComponent;
  let fixture: ComponentFixture<FuncionarioCrearEditarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FuncionarioCrearEditarComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FuncionarioCrearEditarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
