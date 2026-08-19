import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MenuVisualizarComponent } from './menu-visualizar.component';

describe('MenuVisualizarComponent', () => {
  let component: MenuVisualizarComponent;
  let fixture: ComponentFixture<MenuVisualizarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MenuVisualizarComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MenuVisualizarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
