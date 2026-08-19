import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VisualizarImagenCompComponent } from './visualizar-imagen-comp.component';

describe('VisualizarImagenCompComponent', () => {
  let component: VisualizarImagenCompComponent;
  let fixture: ComponentFixture<VisualizarImagenCompComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VisualizarImagenCompComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(VisualizarImagenCompComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
