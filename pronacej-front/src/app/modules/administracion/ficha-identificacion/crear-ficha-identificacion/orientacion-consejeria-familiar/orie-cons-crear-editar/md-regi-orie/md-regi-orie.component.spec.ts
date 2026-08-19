import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MdRegiOrieComponent } from './md-regi-orie.component';

describe('MdRegiOrieComponent', () => {
  let component: MdRegiOrieComponent;
  let fixture: ComponentFixture<MdRegiOrieComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MdRegiOrieComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MdRegiOrieComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
