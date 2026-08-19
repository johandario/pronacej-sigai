import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CrearFuncionarioUsuarioComponent } from './crear-funcionario-usuario.component';

describe('CrearFuncionarioUsuarioComponent', () => {
  let component: CrearFuncionarioUsuarioComponent;
  let fixture: ComponentFixture<CrearFuncionarioUsuarioComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CrearFuncionarioUsuarioComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CrearFuncionarioUsuarioComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
