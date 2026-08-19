import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CrearEditarPlanAsistPostEgresoComponent } from './crear-editar-plan-asist-post-egreso.component';

describe('CrearEditarPlanAsistPostEgresoComponent', () => {
  let component: CrearEditarPlanAsistPostEgresoComponent;
  let fixture: ComponentFixture<CrearEditarPlanAsistPostEgresoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CrearEditarPlanAsistPostEgresoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CrearEditarPlanAsistPostEgresoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
