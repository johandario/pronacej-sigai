import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CrearEditarPtiAbiertoComponent } from './crear-editar-pti-abierto.component';

describe('CrearEditarPtiAbiertoComponent', () => {
  let component: CrearEditarPtiAbiertoComponent;
  let fixture: ComponentFixture<CrearEditarPtiAbiertoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CrearEditarPtiAbiertoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CrearEditarPtiAbiertoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
