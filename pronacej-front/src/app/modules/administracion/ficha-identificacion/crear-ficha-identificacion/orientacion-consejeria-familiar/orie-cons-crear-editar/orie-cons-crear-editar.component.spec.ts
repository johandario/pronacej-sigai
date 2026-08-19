import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OrieConsCrearEditarComponent } from './orie-cons-crear-editar.component';

describe('OrieConsCrearEditarComponent', () => {
  let component: OrieConsCrearEditarComponent;
  let fixture: ComponentFixture<OrieConsCrearEditarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OrieConsCrearEditarComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OrieConsCrearEditarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
