import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EncuestasVerComponent } from './encuestas-ver.component';

describe('EncuestasVerComponent', () => {
  let component: EncuestasVerComponent;
  let fixture: ComponentFixture<EncuestasVerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EncuestasVerComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EncuestasVerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
