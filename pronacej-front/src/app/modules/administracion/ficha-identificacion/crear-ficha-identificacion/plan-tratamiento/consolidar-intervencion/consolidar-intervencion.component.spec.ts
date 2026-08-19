import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ConsolidarIntervencionComponent } from './consolidar-intervencion.component';

describe('ConsolidarIntervencionComponent', () => {
  let component: ConsolidarIntervencionComponent;
  let fixture: ComponentFixture<ConsolidarIntervencionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ConsolidarIntervencionComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ConsolidarIntervencionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
