import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MdRegiSituComponent } from './md-regi-situ.component';

describe('MdRegiSituComponent', () => {
  let component: MdRegiSituComponent;
  let fixture: ComponentFixture<MdRegiSituComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MdRegiSituComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MdRegiSituComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
