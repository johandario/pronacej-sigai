import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SegPtiCerradoComponent } from './seg-pti-cerrado.component';

describe('SegPtiCerradoComponent', () => {
  let component: SegPtiCerradoComponent;
  let fixture: ComponentFixture<SegPtiCerradoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SegPtiCerradoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SegPtiCerradoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
