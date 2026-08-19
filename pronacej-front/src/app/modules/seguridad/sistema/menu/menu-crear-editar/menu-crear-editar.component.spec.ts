import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MenuCrearEditarComponent } from './menu-crear-editar.component';

describe('MenuCrearEditarComponent', () => {
  let component: MenuCrearEditarComponent;
  let fixture: ComponentFixture<MenuCrearEditarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MenuCrearEditarComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MenuCrearEditarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
