import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CrearEditarRegistroLegalComponent } from './crear-editar-registro-legal.component';

describe('CrearEditarRegistroLegalComponent', () => {
  let component: CrearEditarRegistroLegalComponent;
  let fixture: ComponentFixture<CrearEditarRegistroLegalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CrearEditarRegistroLegalComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CrearEditarRegistroLegalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
