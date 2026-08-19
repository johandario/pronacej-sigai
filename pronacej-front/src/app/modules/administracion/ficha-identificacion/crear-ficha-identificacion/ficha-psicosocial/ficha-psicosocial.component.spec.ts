import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FichaPsicosocialComponent } from './ficha-psicosocial.component';

describe('FichaPsicosocialComponent', () => {
  let component: FichaPsicosocialComponent;
  let fixture: ComponentFixture<FichaPsicosocialComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FichaPsicosocialComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FichaPsicosocialComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
