import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SituEconCrearEditarComponent } from './situ-econ-crear-editar.component';

describe('SituEconCrearEditarComponent', () => {
  let component: SituEconCrearEditarComponent;
  let fixture: ComponentFixture<SituEconCrearEditarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SituEconCrearEditarComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SituEconCrearEditarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
