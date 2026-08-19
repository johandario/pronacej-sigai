import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ConstructorEncuestaComponent } from './constructor-encuesta.component';

describe('ConstructorEncuestaComponent', () => {
  let component: ConstructorEncuestaComponent;
  let fixture: ComponentFixture<ConstructorEncuestaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ConstructorEncuestaComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ConstructorEncuestaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
