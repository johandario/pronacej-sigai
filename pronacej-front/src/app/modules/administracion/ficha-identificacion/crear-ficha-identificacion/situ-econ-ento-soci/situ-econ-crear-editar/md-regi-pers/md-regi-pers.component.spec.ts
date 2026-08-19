import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MdRegiPersComponent } from './md-regi-pers.component';

describe('MdRegiPersComponent', () => {
  let component: MdRegiPersComponent;
  let fixture: ComponentFixture<MdRegiPersComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MdRegiPersComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MdRegiPersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
