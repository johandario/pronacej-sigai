import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GestionFugaComponent } from './gestion-fuga.component';

describe('TrasladoComponent', () => {
  let component: GestionFugaComponent;
  let fixture: ComponentFixture<GestionFugaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GestionFugaComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GestionFugaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});