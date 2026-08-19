import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ApreFinaCrearEditarComponent } from './apre-fina-crear-editar.component';

describe('ApreFinaCrearEditarComponent', () => {
  let component: ApreFinaCrearEditarComponent;
  let fixture: ComponentFixture<ApreFinaCrearEditarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ApreFinaCrearEditarComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ApreFinaCrearEditarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
