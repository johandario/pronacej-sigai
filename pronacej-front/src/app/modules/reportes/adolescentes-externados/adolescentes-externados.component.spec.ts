import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdolescentesExternadosComponent } from '../rept-adol-externados/rept-adol-externados.component';

describe('AdolescentesExternadosComponent', () => {
  let component: AdolescentesExternadosComponent;
  let fixture: ComponentFixture<AdolescentesExternadosComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdolescentesExternadosComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdolescentesExternadosComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
