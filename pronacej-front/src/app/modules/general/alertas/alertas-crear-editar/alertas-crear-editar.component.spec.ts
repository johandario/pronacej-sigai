import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AlertasCrearEditarComponent } from './alertas-crear-editar.component';

describe('AlertasCrearEditarComponent', () => {
  let component: AlertasCrearEditarComponent;
  let fixture: ComponentFixture<AlertasCrearEditarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AlertasCrearEditarComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AlertasCrearEditarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
