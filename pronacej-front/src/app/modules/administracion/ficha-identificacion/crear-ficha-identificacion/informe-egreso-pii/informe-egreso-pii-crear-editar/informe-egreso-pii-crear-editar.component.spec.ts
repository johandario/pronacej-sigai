import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InformeEgresoPiiCrearEditarComponent } from './informe-egreso-pii-crear-editar.component';

describe('InformeEgresoPiiCrearEditarComponent', () => {
  let component: InformeEgresoPiiCrearEditarComponent;
  let fixture: ComponentFixture<InformeEgresoPiiCrearEditarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InformeEgresoPiiCrearEditarComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(InformeEgresoPiiCrearEditarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
