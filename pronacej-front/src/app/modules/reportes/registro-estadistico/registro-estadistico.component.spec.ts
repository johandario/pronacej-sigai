import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RegistroEstadisticoComponent } from './registro-estadistico.component';

describe('RegistroEstadisticoComponent', () => {
  let component: RegistroEstadisticoComponent;
  let fixture: ComponentFixture<RegistroEstadisticoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RegistroEstadisticoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RegistroEstadisticoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
