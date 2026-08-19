import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CrearEditarProcesoComponent } from './crear-editar-proceso.component';

describe('CrearEditarProcesoComponent', () => {
  let component: CrearEditarProcesoComponent;
  let fixture: ComponentFixture<CrearEditarProcesoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CrearEditarProcesoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CrearEditarProcesoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
