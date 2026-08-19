import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AuditoriaSistemaVisualizarComponent } from './auditoria-sistema-visualizar.component';

describe('AuditoriaSistemaVisualizarComponent', () => {
  let component: AuditoriaSistemaVisualizarComponent;
  let fixture: ComponentFixture<AuditoriaSistemaVisualizarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AuditoriaSistemaVisualizarComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AuditoriaSistemaVisualizarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
