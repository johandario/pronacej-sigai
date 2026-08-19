import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MdRegiCondComponent } from './md-regi-cond.component';

describe('MdRegiCondComponent', () => {
  let component: MdRegiCondComponent;
  let fixture: ComponentFixture<MdRegiCondComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MdRegiCondComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MdRegiCondComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
