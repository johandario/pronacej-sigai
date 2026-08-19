import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MdRegiFactComponent } from './md-regi-fact.component';

describe('MdRegiFactComponent', () => {
  let component: MdRegiFactComponent;
  let fixture: ComponentFixture<MdRegiFactComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MdRegiFactComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MdRegiFactComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
