import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SituacionEducativaLaboralComponent } from './situacion-educativa-laboral.component';

describe('SituacionEducativaLaboralComponent', () => {
  let component: SituacionEducativaLaboralComponent;
  let fixture: ComponentFixture<SituacionEducativaLaboralComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SituacionEducativaLaboralComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SituacionEducativaLaboralComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
