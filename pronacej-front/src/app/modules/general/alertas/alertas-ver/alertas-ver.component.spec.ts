import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AlertasVerComponent } from './alertas-ver.component';

describe('AlertasVerComponent', () => {
  let component: AlertasVerComponent;
  let fixture: ComponentFixture<AlertasVerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AlertasVerComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AlertasVerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
