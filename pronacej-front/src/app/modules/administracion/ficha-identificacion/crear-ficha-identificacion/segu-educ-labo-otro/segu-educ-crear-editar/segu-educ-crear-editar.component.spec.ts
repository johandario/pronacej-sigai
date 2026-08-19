import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SeguEducCrearEditarComponent } from './segu-educ-crear-editar.component';

describe('SeguEducCrearEditarComponent', () => {
  let component: SeguEducCrearEditarComponent;
  let fixture: ComponentFixture<SeguEducCrearEditarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SeguEducCrearEditarComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SeguEducCrearEditarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
