import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InformeEgresoPiiComponent } from './informe-egreso-pii.component';

describe('InformeEgresoPiiComponent', () => {
  let component: InformeEgresoPiiComponent;
  let fixture: ComponentFixture<InformeEgresoPiiComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InformeEgresoPiiComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(InformeEgresoPiiComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
