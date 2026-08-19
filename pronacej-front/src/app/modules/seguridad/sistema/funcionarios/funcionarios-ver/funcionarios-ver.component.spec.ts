import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FuncionariosVerComponent } from './funcionarios-ver.component';

describe('FuncionariosVerComponent', () => {
  let component: FuncionariosVerComponent;
  let fixture: ComponentFixture<FuncionariosVerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FuncionariosVerComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FuncionariosVerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
