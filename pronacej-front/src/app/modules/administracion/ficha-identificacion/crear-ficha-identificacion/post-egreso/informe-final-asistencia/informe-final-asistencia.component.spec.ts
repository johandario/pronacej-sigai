import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InformeFinalAsistenciaComponent } from './informe-final-asistencia.component';

describe('InformeFinalAsistenciaComponent', () => {
  let component: InformeFinalAsistenciaComponent;
  let fixture: ComponentFixture<InformeFinalAsistenciaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InformeFinalAsistenciaComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(InformeFinalAsistenciaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
