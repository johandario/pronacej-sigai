import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EvalSeguSociComponent } from './eval-segu-soci.component';

describe('EvalSeguSociComponent', () => {
  let component: EvalSeguSociComponent;
  let fixture: ComponentFixture<EvalSeguSociComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EvalSeguSociComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EvalSeguSociComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
