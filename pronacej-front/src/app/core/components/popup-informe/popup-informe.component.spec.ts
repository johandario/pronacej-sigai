import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PopupInformeComponent } from './popup-informe.component';

describe('PopupInformeComponent', () => {
  let component: PopupInformeComponent;
  let fixture: ComponentFixture<PopupInformeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PopupInformeComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PopupInformeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
