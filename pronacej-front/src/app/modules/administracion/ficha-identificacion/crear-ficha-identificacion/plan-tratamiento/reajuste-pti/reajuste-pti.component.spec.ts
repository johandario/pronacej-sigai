import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReajustePtiComponent } from './reajuste-pti.component';

describe('ReajustePtiComponent', () => {
  let component: ReajustePtiComponent;
  let fixture: ComponentFixture<ReajustePtiComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReajustePtiComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ReajustePtiComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
