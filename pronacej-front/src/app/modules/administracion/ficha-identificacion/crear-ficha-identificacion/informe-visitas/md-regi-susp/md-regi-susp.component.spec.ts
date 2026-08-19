import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MdRegiSuspComponent } from './md-regi-susp.component';

describe('MdRegiSuspComponent', () => {
  let component: MdRegiSuspComponent;
  let fixture: ComponentFixture<MdRegiSuspComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MdRegiSuspComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MdRegiSuspComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
