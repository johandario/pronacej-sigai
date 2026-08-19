import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MdRegiCritComponent } from './md-regi-crit.component';

describe('MdRegiCritComponent', () => {
  let component: MdRegiCritComponent;
  let fixture: ComponentFixture<MdRegiCritComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MdRegiCritComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MdRegiCritComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
