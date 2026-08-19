import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GestionPtiComponent } from './gestion-pti.component';

describe('GestionPtiComponent', () => {
  let component: GestionPtiComponent;
  let fixture: ComponentFixture<GestionPtiComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GestionPtiComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GestionPtiComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
