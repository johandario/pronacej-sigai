import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SituacionRiesgoSocialComponent } from './situacion-riesgo-social.component';

describe('SituacionRiesgoSocialComponent', () => {
  let component: SituacionRiesgoSocialComponent;
  let fixture: ComponentFixture<SituacionRiesgoSocialComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SituacionRiesgoSocialComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SituacionRiesgoSocialComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
