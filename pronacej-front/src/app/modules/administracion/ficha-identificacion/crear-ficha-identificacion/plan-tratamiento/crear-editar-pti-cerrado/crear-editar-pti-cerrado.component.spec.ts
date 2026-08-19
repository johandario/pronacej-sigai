import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CrearEditarPtiCerradoComponent } from './crear-editar-pti-cerrado.component';

describe('CrearEditarPtiCerradoComponent', () => {
  let component: CrearEditarPtiCerradoComponent;
  let fixture: ComponentFixture<CrearEditarPtiCerradoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CrearEditarPtiCerradoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CrearEditarPtiCerradoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
