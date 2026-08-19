import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SituacionRiesgoSocialCrearEditarComponent } from './situacion-riesgo-social-crear-editar.component';

describe('SituacionRiesgoSocialCrearEditarComponent', () => {
  let component: SituacionRiesgoSocialCrearEditarComponent;
  let fixture: ComponentFixture<SituacionRiesgoSocialCrearEditarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SituacionRiesgoSocialCrearEditarComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SituacionRiesgoSocialCrearEditarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
