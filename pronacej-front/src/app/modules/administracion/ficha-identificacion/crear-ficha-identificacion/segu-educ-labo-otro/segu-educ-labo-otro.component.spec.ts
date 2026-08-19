import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SeguEducLaboOtroComponent } from './segu-educ-labo-otro.component';

describe('SeguEducLaboOtroComponent', () => {
  let component: SeguEducLaboOtroComponent;
  let fixture: ComponentFixture<SeguEducLaboOtroComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SeguEducLaboOtroComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SeguEducLaboOtroComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
