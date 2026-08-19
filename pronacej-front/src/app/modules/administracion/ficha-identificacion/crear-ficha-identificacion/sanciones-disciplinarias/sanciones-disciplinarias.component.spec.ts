import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SancionesDisciplinariasComponent } from './sanciones-disciplinarias.component';

describe('SancionesDisciplinariasComponent', () => {
  let component: SancionesDisciplinariasComponent;
  let fixture: ComponentFixture<SancionesDisciplinariasComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SancionesDisciplinariasComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SancionesDisciplinariasComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
