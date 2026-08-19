import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RolCrearEditarComponent } from './rol-crear-editar.component';

describe('RolCrearEditarComponent', () => {
  let component: RolCrearEditarComponent;
  let fixture: ComponentFixture<RolCrearEditarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RolCrearEditarComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RolCrearEditarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
