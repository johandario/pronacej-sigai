import { ComponentFixture, TestBed } from '@angular/core/testing';

import { JerarquiaDialogComponent } from './jerarquia-dialog.component';

describe('JerarquiaDialogComponent', () => {
  let component: JerarquiaDialogComponent;
  let fixture: ComponentFixture<JerarquiaDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [JerarquiaDialogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(JerarquiaDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
