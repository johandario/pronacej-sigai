import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InformacionLocalidadComponent } from './informacion-localidad.component';

describe('InformacionLocalidadComponent', () => {
  let component: InformacionLocalidadComponent;
  let fixture: ComponentFixture<InformacionLocalidadComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InformacionLocalidadComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(InformacionLocalidadComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
