import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InformeTecnicoSustentatorioCrearEditarComponent } from './informe-tecnico-sustentatorio-crear-editar.component';

describe('InformeTecnicoSustentatorioCrearEditarComponent', () => {
  let component: InformeTecnicoSustentatorioCrearEditarComponent;
  let fixture: ComponentFixture<InformeTecnicoSustentatorioCrearEditarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InformeTecnicoSustentatorioCrearEditarComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(InformeTecnicoSustentatorioCrearEditarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
