import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdministrarEcComponent } from './administrar-ec.component';

describe('AdministrarEcComponent', () => {
  let component: AdministrarEcComponent;
  let fixture: ComponentFixture<AdministrarEcComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdministrarEcComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdministrarEcComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
