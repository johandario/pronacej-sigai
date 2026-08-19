import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FuncionarioAccionesComponent } from './funcionario-acciones.component';

describe('FuncionarioAccionesComponent', () => {
  let component: FuncionarioAccionesComponent;
  let fixture: ComponentFixture<FuncionarioAccionesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FuncionarioAccionesComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FuncionarioAccionesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
