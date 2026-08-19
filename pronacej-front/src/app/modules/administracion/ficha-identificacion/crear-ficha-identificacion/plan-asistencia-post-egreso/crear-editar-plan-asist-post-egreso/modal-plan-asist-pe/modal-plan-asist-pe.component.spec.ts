import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModalPlanAsistPeComponent } from './modal-plan-asist-pe.component';

describe('ModalPlanAsistPeComponent', () => {
  let component: ModalPlanAsistPeComponent;
  let fixture: ComponentFixture<ModalPlanAsistPeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModalPlanAsistPeComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ModalPlanAsistPeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
