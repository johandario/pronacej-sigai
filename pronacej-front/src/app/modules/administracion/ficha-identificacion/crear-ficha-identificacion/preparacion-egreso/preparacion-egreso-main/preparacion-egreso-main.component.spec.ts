import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PreparacionEgresoMainComponent } from './preparacion-egreso-main.component';

describe('PreparacionEgresoMainComponent', () => {
  let component: PreparacionEgresoMainComponent;
  let fixture: ComponentFixture<PreparacionEgresoMainComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PreparacionEgresoMainComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PreparacionEgresoMainComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
