import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EvaluacionSocialComponent } from './evaluacion-social.component';

describe('EvaluacionSocialComponent', () => {
  let component: EvaluacionSocialComponent;
  let fixture: ComponentFixture<EvaluacionSocialComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EvaluacionSocialComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EvaluacionSocialComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
