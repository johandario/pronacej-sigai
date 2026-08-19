import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EstudiosCrearEditarComponent } from './estudios-crear-editar.component';

describe('EstudiosCrearEditarComponent', () => {
  let component: EstudiosCrearEditarComponent;
  let fixture: ComponentFixture<EstudiosCrearEditarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EstudiosCrearEditarComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EstudiosCrearEditarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
