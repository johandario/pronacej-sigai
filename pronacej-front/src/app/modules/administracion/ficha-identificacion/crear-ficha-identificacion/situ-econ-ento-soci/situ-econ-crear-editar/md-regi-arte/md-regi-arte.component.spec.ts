import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MdRegiArteComponent } from './md-regi-arte.component';

describe('MdRegiArteComponent', () => {
  let component: MdRegiArteComponent;
  let fixture: ComponentFixture<MdRegiArteComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MdRegiArteComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MdRegiArteComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
