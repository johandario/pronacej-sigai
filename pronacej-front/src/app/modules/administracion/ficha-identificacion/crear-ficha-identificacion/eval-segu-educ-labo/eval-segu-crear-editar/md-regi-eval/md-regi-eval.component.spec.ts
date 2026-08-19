import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MdRegiEvalComponent } from './md-regi-eval.component';

describe('MdRegiEvalComponent', () => {
  let component: MdRegiEvalComponent;
  let fixture: ComponentFixture<MdRegiEvalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MdRegiEvalComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MdRegiEvalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
