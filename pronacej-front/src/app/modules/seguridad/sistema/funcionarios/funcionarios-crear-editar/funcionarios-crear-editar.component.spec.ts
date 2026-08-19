import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FuncionariosCrearEditarComponent } from './funcionarios-crear-editar.component';

describe('FuncionariosCrearEditarComponent', () => {
  let component: FuncionariosCrearEditarComponent;
  let fixture: ComponentFixture<FuncionariosCrearEditarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FuncionariosCrearEditarComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FuncionariosCrearEditarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
