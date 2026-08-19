import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PopupDocumentosComponent } from './popup-documentos.component';

describe('PopupDocumentosComponent', () => {
  let component: PopupDocumentosComponent;
  let fixture: ComponentFixture<PopupDocumentosComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PopupDocumentosComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PopupDocumentosComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
