import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InformesCrearEditarComponent } from './informes-crear-editar.component';

describe('InformesCrearEditarComponent', () => {
  let component: InformesCrearEditarComponent;
  let fixture: ComponentFixture<InformesCrearEditarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InformesCrearEditarComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(InformesCrearEditarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
