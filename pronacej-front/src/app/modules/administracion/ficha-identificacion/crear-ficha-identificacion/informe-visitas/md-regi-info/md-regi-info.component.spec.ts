import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MdRegiInfoComponent } from './md-regi-info.component';

describe('MdRegiInfoComponent', () => {
  let component: MdRegiInfoComponent;
  let fixture: ComponentFixture<MdRegiInfoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MdRegiInfoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MdRegiInfoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
