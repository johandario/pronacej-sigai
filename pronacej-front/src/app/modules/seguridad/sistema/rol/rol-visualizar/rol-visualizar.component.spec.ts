import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RolVisualizarComponent } from './rol-visualizar.component';

describe('RolVisualizarComponent', () => {
  let component: RolVisualizarComponent;
  let fixture: ComponentFixture<RolVisualizarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RolVisualizarComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RolVisualizarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
