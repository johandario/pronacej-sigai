import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EvalSeguEducLaboComponent } from './eval-segu-educ-labo.component';

describe('EvalSeguEducLaboComponent', () => {
  let component: EvalSeguEducLaboComponent;
  let fixture: ComponentFixture<EvalSeguEducLaboComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EvalSeguEducLaboComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EvalSeguEducLaboComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
