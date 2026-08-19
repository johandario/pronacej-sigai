import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EntregaRecepcionUniformesArticulosComponent } from './entrega-recepcion-uniformes-articulos.component';

describe('EntregaRecepcionUniformesArticulosComponent', () => {
  let component: EntregaRecepcionUniformesArticulosComponent;
  let fixture: ComponentFixture<EntregaRecepcionUniformesArticulosComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EntregaRecepcionUniformesArticulosComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EntregaRecepcionUniformesArticulosComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
