import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NotificacionesVerComponent } from './notificaciones-ver.component';

describe('NotificacionesVerComponent', () => {
  let component: NotificacionesVerComponent;
  let fixture: ComponentFixture<NotificacionesVerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NotificacionesVerComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(NotificacionesVerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
