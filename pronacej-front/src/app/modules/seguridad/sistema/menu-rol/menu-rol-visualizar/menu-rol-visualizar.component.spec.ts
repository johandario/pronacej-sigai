import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MenuRolVisualizarComponent } from './menu-rol-visualizar.component';

describe('MenuRolVisualizarComponent', () => {
  let component: MenuRolVisualizarComponent;
  let fixture: ComponentFixture<MenuRolVisualizarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MenuRolVisualizarComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MenuRolVisualizarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
