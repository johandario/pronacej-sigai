import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FuncionarioVisualizarComponent } from './funcionario-visualizar.component';

describe('FuncionarioVisualizarComponent', () => {
  let component: FuncionarioVisualizarComponent;
  let fixture: ComponentFixture<FuncionarioVisualizarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FuncionarioVisualizarComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FuncionarioVisualizarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
