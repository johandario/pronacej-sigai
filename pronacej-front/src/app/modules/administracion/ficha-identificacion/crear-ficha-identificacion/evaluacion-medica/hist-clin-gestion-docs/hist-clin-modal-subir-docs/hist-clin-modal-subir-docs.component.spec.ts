import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HistClinModalSubirDocsComponent } from './hist-clin-modal-subir-docs.component';

describe('HistClinModalSubirDocsComponent', () => {
  let component: HistClinModalSubirDocsComponent;
  let fixture: ComponentFixture<HistClinModalSubirDocsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HistClinModalSubirDocsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(HistClinModalSubirDocsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
