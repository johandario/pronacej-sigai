import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SituacionCrearEditarComponent } from './situacion-crear-editar.component';

describe('SituacionCrearEditarComponent', () => {
  let component: SituacionCrearEditarComponent;
  let fixture: ComponentFixture<SituacionCrearEditarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SituacionCrearEditarComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SituacionCrearEditarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
