import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HistClinGestionDocsComponent } from './hist-clin-gestion-docs.component';

describe('HistClinGestionDocsComponent', () => {
  let component: HistClinGestionDocsComponent;
  let fixture: ComponentFixture<HistClinGestionDocsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HistClinGestionDocsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(HistClinGestionDocsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
