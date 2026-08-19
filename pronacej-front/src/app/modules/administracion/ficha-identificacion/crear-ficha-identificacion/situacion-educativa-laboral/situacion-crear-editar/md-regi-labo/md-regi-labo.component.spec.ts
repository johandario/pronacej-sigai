import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MdRegiLaboComponent } from './md-regi-labo.component';

describe('MdRegiLaboComponent', () => {
  let component: MdRegiLaboComponent;
  let fixture: ComponentFixture<MdRegiLaboComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MdRegiLaboComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MdRegiLaboComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
