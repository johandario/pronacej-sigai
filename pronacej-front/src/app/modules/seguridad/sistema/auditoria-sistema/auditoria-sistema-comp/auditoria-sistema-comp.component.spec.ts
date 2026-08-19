import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AuditoriaSistemaCompComponent } from './auditoria-sistema-comp.component';

describe('AuditoriaSistemaCompComponent', () => {
  let component: AuditoriaSistemaCompComponent;
  let fixture: ComponentFixture<AuditoriaSistemaCompComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AuditoriaSistemaCompComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AuditoriaSistemaCompComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
