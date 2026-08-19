import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InformeTecnicoSustentatorioComponent } from './informe-tecnico-sustentatorio.component';

describe('InformeTecnicoSustentatorioComponent', () => {
  let component: InformeTecnicoSustentatorioComponent;
  let fixture: ComponentFixture<InformeTecnicoSustentatorioComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InformeTecnicoSustentatorioComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(InformeTecnicoSustentatorioComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
