import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RegistrosLegalesComponent } from './registros-legales.component';

describe('RegistrosLegalesComponent', () => {
  let component: RegistrosLegalesComponent;
  let fixture: ComponentFixture<RegistrosLegalesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RegistrosLegalesComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RegistrosLegalesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
