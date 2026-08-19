import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VisualizarJsonModalComponent } from './visualizar-json-modal.component';

describe('VisualizarJsonModalComponent', () => {
  let component: VisualizarJsonModalComponent;
  let fixture: ComponentFixture<VisualizarJsonModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VisualizarJsonModalComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(VisualizarJsonModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
