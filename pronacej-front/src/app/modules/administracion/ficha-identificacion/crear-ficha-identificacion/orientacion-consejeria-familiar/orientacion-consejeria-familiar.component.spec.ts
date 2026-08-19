import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OrientacionConsejeriaFamiliarComponent } from './orientacion-consejeria-familiar.component';

describe('OrientacionConsejeriaFamiliarComponent', () => {
  let component: OrientacionConsejeriaFamiliarComponent;
  let fixture: ComponentFixture<OrientacionConsejeriaFamiliarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OrientacionConsejeriaFamiliarComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OrientacionConsejeriaFamiliarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
