import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CrearEditarInformeFinalAbiertoComponent } from './crear-editar-informe-final-abierto.component';

describe('CrearEditarInformeFinalAbiertoComponent', () => {
  let component: CrearEditarInformeFinalAbiertoComponent;
  let fixture: ComponentFixture<CrearEditarInformeFinalAbiertoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CrearEditarInformeFinalAbiertoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CrearEditarInformeFinalAbiertoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
