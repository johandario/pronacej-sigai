import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ActividadIntervencionDialogComponent } from './actividad-intervencion-dialog.component';

describe('ActividadIntervencionDialogComponent', () => {
  let component: ActividadIntervencionDialogComponent;
  let fixture: ComponentFixture<ActividadIntervencionDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ActividadIntervencionDialogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ActividadIntervencionDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
